package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cloud.hospital.system.MyException.BusinessException;
import com.cloud.hospital.system.Utils.IdGenerator;
import com.cloud.hospital.system.common.OrderStatusEnum;
import com.cloud.hospital.system.config.RabbitMQConfig;
import com.cloud.hospital.system.constant.ExceptionCode;
import com.cloud.hospital.system.constant.RedisKeyPrefix;
import com.cloud.hospital.system.dto.BookOrderDTO;
import com.cloud.hospital.system.entity.RegistrationOrder;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.mapper.RegistrationOrderMapper;
import com.cloud.hospital.system.service.IRegistrationOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.hospital.system.service.IScheduleService;
import com.cloud.hospital.system.task.ScheduleInventoryWarmupTask;
import com.cloud.hospital.system.vo.OrderResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

        Long patientId =   bookOrderDTO.getPatientId();
        Long scheduleId =   bookOrderDTO.getScheduleId();

        log.info(">>> 收到下单请求: 患者ID={}, 排班ID={}", patientId, scheduleId);


        String redisKey = RedisKeyPrefix.SCHEDULE_INV_KEY_PREFIX + scheduleId;

        //检查是否命中缓存
        checkIfHitCache(redisKey, scheduleId);

        Long remainNum = stringRedisTemplate.opsForValue().decrement(redisKey);
        if(remainNum ==null || remainNum<0){
            stringRedisTemplate.opsForValue().increment(redisKey);
            log.warn("下单失败: 剩余库存不足, 排班ID={}", bookOrderDTO.getScheduleId());
            throw new BusinessException(ExceptionCode.SOURCE_NOT_ENOUGH,"号抢没了,换一个号源试试");
        }else{
            log.info(">>> [预扣成功] 剩余库存充足, 排班ID={}", scheduleId);
        }

        //下面开始更新数据库库存

        try {
            //从缓存中更新, 并更新数据库
            OrderResultVO orderResultVO = checkAndUpdateDB(scheduleId, patientId);
                return  orderResultVO;

        } catch (Exception e) {
            log.error(">>> [下单异常] 数据库落库失败，回滚Redis库存，排班ID: {}", scheduleId, e);
            stringRedisTemplate.opsForValue().increment(redisKey);

            // 重新抛出异常，让 Spring 的 @Transactional 帮我们把 MySQL 的半截子数据回滚掉
            throw e;
        }
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

    private OrderResultVO checkAndUpdateDB(Long scheduleId,Long patientId){
        // 1. 先查询排班是否存在
        Schedule schedule = scheduleService.getById(scheduleId);
        if (schedule == null) {
            log.warn("下单失败: 排班记录不存在, ID={}", scheduleId);
            throw new RuntimeException("该排班不存在");
        }

        // 2. 核心：乐观锁扣减库存
        // UPDATE schedule SET available_num = available_num - 1, version = version + 1
        // WHERE id = ? AND version = ? AND available_num > 0
        LambdaUpdateWrapper<Schedule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("available_num = available_num - 1, version = version + 1")
                .eq(Schedule::getId, scheduleId)
                .eq(Schedule::getVersion, schedule.getVersion()) // 乐观锁：版本号匹配
                .gt(Schedule::getAvailableNum, 0);               // 兜底：余号必须大于0

        boolean updateSuccess = scheduleService.update(updateWrapper);

        if (!updateSuccess) {
            log.error("抢号失败: 触发高并发冲突或号源已抢光, 排班ID={}", scheduleId);
            throw new BusinessException(ExceptionCode.SOURCE_NOT_ENOUGH,"号抢没了,换一个号源试试");
        }


        log.info("库存扣减成功，开始生成订单...");
        // 3. 生成全局唯一的订单号 (雪花算法)
        long snowflakeId = idGenerator.nextId();
        String orderNo = "ORD" + snowflakeId;
        log.info(">>> 订单号生成成功: {}", orderNo);


        RegistrationOrder order = RegistrationOrder.builder()
                .orderNo(orderNo)
                .id(snowflakeId)
                .patientId(patientId)
                .scheduleId(schedule.getId())
                .departmentId(schedule.getDepartmentId())
                .doctorId(schedule.getDoctorId())
                .reserveTime(schedule.getWorkDate().atStartOfDay())
                .status(OrderStatusEnum.WAITING_PAY)
                .payAmount(schedule.getAmount())
                .createTime(LocalDateTime.now())
                .build();
        this.save(order);

        Long orderId = order.getId();

        //把订单ID发送到延迟队列
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_TTL_EXCHANGE,
                RabbitMQConfig.ORDER_TTL_ROUTING_KEY,
                String.valueOf(orderId)
        );

        log.info("订单生成成功，订单ID: {}", orderId);


        // 5. 使用静态工厂或 Builder 模式返回 VO
        return OrderResultVO.builder()
                .orderNo(order.getOrderNo())
                .payAmount(order.getPayAmount())
                .status(order.getStatus().getCode()) // 返回枚举对应的数值 0
                .build();

    }
}
