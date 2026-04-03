package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cloud.hospital.system.MyException.BusinessException;
import com.cloud.hospital.system.Utils.IdGenerator;
import com.cloud.hospital.system.common.OrderStatusEnum;
import com.cloud.hospital.system.config.RabbitMQConfig;
import com.cloud.hospital.system.constant.ExceptionCode;
import com.cloud.hospital.system.constant.RedisKeyPrefix;
import com.cloud.hospital.system.context.AuthContext;
import com.cloud.hospital.system.dto.BookOrderDTO;
import com.cloud.hospital.system.entity.RegistrationOrder;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.mapper.RegistrationOrderMapper;
import com.cloud.hospital.system.service.IRegistrationOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.hospital.system.service.IScheduleService;
import com.cloud.hospital.system.vo.OrderAsyncResultVO;
import com.cloud.hospital.system.vo.OrderResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 挂号订单表 服务实现类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Slf4j
@Service
public class RegistrationOrderServiceImpl extends ServiceImpl<RegistrationOrderMapper, RegistrationOrder> implements IRegistrationOrderService {

    private static final DefaultRedisScript<Long> PRE_DEDUCT_SCRIPT = new DefaultRedisScript<>(
            "local scheduleInvKey = KEYS[1]\n" +
            "local patientLimitKey = KEYS[2]\n" +
            "local patientId = ARGV[1]\n" +
            "local expireTime = ARGV[2]\n" +
            "\n" +
            "-- 1. 检查是否已经抢过（防重复下单）\n" +
            "local hasBooked = redis.call('SISMEMBER', patientLimitKey, patientId)\n" +
            "if hasBooked == 1 then\n" +
            "    return -4 -- 重复下单\n" +
            "end\n" +
            "\n" +
            "-- 2. 检查库存是否存在\n" +
            "local v = redis.call('GET', scheduleInvKey)\n" +
            "if not v then return -2 end\n" +
            "\n" +
            "-- 3. 检查库存是否充足\n" +
            "local n = tonumber(v)\n" +
            "if not n then return -3 end\n" +
            "if n <= 0 then return -1 end\n" +
            "\n" +
            "-- 4. 扣减库存\n" +
            "n = redis.call('DECR', scheduleInvKey)\n" +
            "\n" +
            "-- 5. 记录该患者已抢成功，并设置过期时间（例如一天）\n" +
            "redis.call('SADD', patientLimitKey, patientId)\n" +
            "redis.call('EXPIRE', patientLimitKey, expireTime)\n" +
            "\n" +
            "return n\n",
            Long.class
    );
    private static final Duration ORDER_RESULT_CACHE_TTL = Duration.ofMinutes(10);

    @Autowired
    private IScheduleService scheduleService;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResultVO bookOrder(BookOrderDTO bookOrderDTO) {

        Long patientId = AuthContext.getPatientId();
        if (patientId == null) {
            throw new RuntimeException("请先登录");
        }
        Long scheduleId =   bookOrderDTO.getScheduleId();

        log.info(">>> 收到下单请求: 患者ID={}, 排班ID={}", patientId, scheduleId);


        String redisKey = RedisKeyPrefix.SCHEDULE_INV_KEY_PREFIX + scheduleId;

        //检查是否命中缓存
        checkIfHitCache(redisKey, scheduleId);

        Long remainNum = preDeductWithLua(redisKey, scheduleId, patientId);
        if (remainNum == null) {
            throw new BusinessException(ExceptionCode.SYSTEM_BUSY, "当前抢号人数过多，系统繁忙，请重试！");
        }
        if (remainNum == -4) {
            log.warn("下单失败: 重复下单, 患者ID={}, 排班ID={}", patientId, scheduleId);
            throw new BusinessException(ExceptionCode.SYSTEM_BUSY, "您已预约过该排班，请勿重复下单！");
        }
        if (remainNum < 0) {
            log.warn("下单失败: 剩余库存不足, 排班ID={}", bookOrderDTO.getScheduleId());
            throw new BusinessException(ExceptionCode.SOURCE_NOT_ENOUGH, "号抢没了,换一个号源试试");
        }

        log.info(">>> [预扣成功] 剩余库存充足, 排班ID={}", scheduleId);

        Schedule schedule = scheduleService.getById(scheduleId);
        if (schedule == null) {
            rollbackRedisDeduct(redisKey, scheduleId, patientId);
            throw new RuntimeException("该排班不存在");
        }

        long snowflakeId = idGenerator.nextId();
        String orderNo = "ORD" + snowflakeId;

        String createOrderMessage = snowflakeId + "," + orderNo + "," + patientId + "," + scheduleId;
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_CREATE_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATE_ROUTING_KEY,
                    createOrderMessage
            );
            stringRedisTemplate.opsForValue().set(
                    RedisKeyPrefix.ORDER_RESULT_KEY_PREFIX + orderNo,
                    "PROCESSING",
                    ORDER_RESULT_CACHE_TTL
            );
        } catch (Exception e) {
            log.error(">>> [下单异常] 下单消息投递失败，回滚Redis库存，排班ID: {}", scheduleId, e);
            rollbackRedisDeduct(redisKey, scheduleId, patientId);
            stringRedisTemplate.opsForValue().set(
                    RedisKeyPrefix.ORDER_RESULT_KEY_PREFIX + orderNo,
                    "FAILED",
                    ORDER_RESULT_CACHE_TTL
            );
            throw new BusinessException(ExceptionCode.SYSTEM_BUSY, "系统繁忙，请稍后重试");
        }

        return OrderResultVO.builder()
                .orderNo(orderNo)
                .payAmount(schedule.getAmount())
                .status(OrderStatusEnum.WAITING_PAY.getCode())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        if (orderId == null) {
            throw new RuntimeException("订单ID不能为空");
        }

        RegistrationOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        Long currentPatientId = AuthContext.getPatientId();
        if (currentPatientId == null) {
            throw new RuntimeException("请先登录");
        }
        if (!currentPatientId.equals(order.getPatientId())) {
            throw new RuntimeException("无权操作该订单");
        }

        if (order.getStatus() == OrderStatusEnum.CANCELLED) {
            return;
        }
        if (order.getStatus() != OrderStatusEnum.WAITING_PAY) {
            throw new RuntimeException("当前订单状态无法取消");
        }

        int updated = this.baseMapper.cancelOrderIfUnpaid(orderId);
        if (updated <= 0) {
            return;
        }

        releaseScheduleStock(order.getScheduleId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId) {
        if (orderId == null) {
            throw new RuntimeException("订单ID不能为空");
        }

        RegistrationOrder order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        Long currentPatientId = AuthContext.getPatientId();
        if (currentPatientId == null) {
            throw new RuntimeException("请先登录");
        }
        if (!currentPatientId.equals(order.getPatientId())) {
            throw new RuntimeException("无权操作该订单");
        }

        if (order.getStatus() == OrderStatusEnum.PAID) {
            return;
        }
        if (order.getStatus() == OrderStatusEnum.CANCELLED) {
            throw new RuntimeException("订单已取消，无法支付");
        }
        if (order.getStatus() != OrderStatusEnum.WAITING_PAY) {
            throw new RuntimeException("当前订单状态无法支付");
        }

        LambdaUpdateWrapper<RegistrationOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(RegistrationOrder::getStatus, OrderStatusEnum.PAID)
                .eq(RegistrationOrder::getId, orderId)
                .eq(RegistrationOrder::getStatus, OrderStatusEnum.WAITING_PAY);

        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new RuntimeException("订单状态更新失败");
        }
    }

    @Override
    public OrderAsyncResultVO queryOrderResult(String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw new RuntimeException("订单号不能为空");
        }

        RegistrationOrder order = this.getOne(new LambdaQueryWrapper<RegistrationOrder>()
                .eq(RegistrationOrder::getOrderNo, orderNo)
                .last("limit 1"));
        Long currentPatientId = AuthContext.getPatientId();
        if (currentPatientId == null) {
            throw new RuntimeException("请先登录");
        }
        if (order != null) {
            if (!currentPatientId.equals(order.getPatientId())) {
                throw new RuntimeException("无权查看该订单");
            }
            return OrderAsyncResultVO.builder()
                    .orderNo(order.getOrderNo())
                    .processState("SUCCESS")
                    .status(order.getStatus().getCode())
                    .payAmount(order.getPayAmount())
                    .message("下单成功")
                    .build();
        }

        String cachedState = stringRedisTemplate.opsForValue().get(RedisKeyPrefix.ORDER_RESULT_KEY_PREFIX + orderNo);
        if ("PROCESSING".equals(cachedState)) {
            return OrderAsyncResultVO.builder()
                    .orderNo(orderNo)
                    .processState("PROCESSING")
                    .message("排队处理中，请稍后刷新")
                    .build();
        }
        if ("FAILED".equals(cachedState)) {
            return OrderAsyncResultVO.builder()
                    .orderNo(orderNo)
                    .processState("FAILED")
                    .message("下单失败，请重试")
                    .build();
        }

        return OrderAsyncResultVO.builder()
                .orderNo(orderNo)
                .processState("FAILED")
                .message("订单不存在或处理结果已过期")
                .build();
    }

    private Long preDeductWithLua(String redisKey, Long scheduleId, Long patientId) {
        String patientLimitKey = "cloud_hospital:schedule:booked:" + scheduleId;
        // 默认记录一天（86400秒）
        String expireTime = "86400";
        
        Long result = stringRedisTemplate.execute(
                PRE_DEDUCT_SCRIPT, 
                java.util.Arrays.asList(redisKey, patientLimitKey),
                String.valueOf(patientId),
                expireTime
        );   
        
        if (result != null && result == -2) {
            checkIfHitCache(redisKey, scheduleId);
            result = stringRedisTemplate.execute(
                    PRE_DEDUCT_SCRIPT, 
                    java.util.Arrays.asList(redisKey, patientLimitKey),
                    String.valueOf(patientId),
                    expireTime
            );
        }
        return result;
    }

    private void rollbackRedisDeduct(String redisKey, Long scheduleId, Long patientId) {
        stringRedisTemplate.opsForValue().increment(redisKey);
        String patientLimitKey = "cloud_hospital:schedule:booked:" + scheduleId;
        stringRedisTemplate.opsForSet().remove(patientLimitKey, String.valueOf(patientId));
    }

    private void releaseScheduleStock(Long scheduleId) {
        if (scheduleId == null) {
            return;
        }

        LambdaUpdateWrapper<Schedule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("available_num = available_num + 1, version = version + 1")
                .eq(Schedule::getId, scheduleId)
                .apply("available_num < total_num");

        scheduleService.update(updateWrapper);

        Schedule schedule = scheduleService.getById(scheduleId);
        String redisKey = RedisKeyPrefix.SCHEDULE_INV_KEY_PREFIX + scheduleId;
        if (schedule == null) {
            stringRedisTemplate.delete(redisKey);
            return;
        }
        stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(schedule.getAvailableNum()));
    }

    private void checkIfHitCache(String redisKey, Long scheduleId) {
        Boolean hasKey = stringRedisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(hasKey)) {
            return;
        }
        // 走到这里说明没缓存。准备加锁！
        // 定义一把专门用于重建这个排班缓存的“锁”
        String lockKey = "lock:build_cache:" + scheduleId;

        // 尝试获取锁：SETNX操作。只有第一个到达的线程能返回 true，其余返回 false。
        // 设置 5 秒过期时间，防止拿到锁的线程突然死机导致死锁
        Boolean getLock = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", 5, TimeUnit.SECONDS);

        //止呕
        if (Boolean.TRUE.equals(getLock)) {

            // ！！！第二重检查（双重检查锁 DCL）！！！
            // 拿到锁之后，必须再看一眼缓存是不是已经被前面刚释放锁的线程给建好了
            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(redisKey))) {

                try {
                    // 赶紧去 MySQL 里查一下真实的底牌
                    Schedule schedule = scheduleService.getById(scheduleId);
                    if (schedule == null) {
                        throw new RuntimeException("该排班不存在！");
                    }

                    // 查到真实余号后，火速补进 Redis，亡羊补牢
                    stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(schedule.getAvailableNum()));

                    // 💡 极端情况判断：如果数据库里查出来也确实是 0，那不用费劲往下走了，直接拒绝
                    if (schedule.getAvailableNum() <= 0) {
                        throw new RuntimeException("手慢了，该医生的号源已抢空！");
                    }
                } finally {
                    //无论抢没抢到都要释放锁
                    stringRedisTemplate.delete(lockKey);
                }
            }
        }else{
            //如果拿到锁了但是缓存已经被前面的线程存了
            //// 没有拿到锁的线程怎么办？
            //            // 在抢票/抢号这种高并发场景下，最优雅的做法是直接抛异常，让用户点重试，实现“快速失败（Fail-fast）”
                       log.warn(">>> [获取锁失败] 其他线程正在重建缓存，拦截本次请求，排班ID: {}", scheduleId);
                        throw new BusinessException(ExceptionCode.SYSTEM_BUSY, "当前抢号人数过多，系统繁忙，请重试！");
        }
        }

}
