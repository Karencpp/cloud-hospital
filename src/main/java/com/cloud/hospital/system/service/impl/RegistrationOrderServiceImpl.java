package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cloud.hospital.system.MyException.BusinessException;
import com.cloud.hospital.system.Utils.IdGenerator;
import com.cloud.hospital.system.common.OrderStatusEnum;
import com.cloud.hospital.system.constant.ExceptionCode;
import com.cloud.hospital.system.dto.BookOrderDTO;
import com.cloud.hospital.system.entity.RegistrationOrder;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.mapper.RegistrationOrderMapper;
import com.cloud.hospital.system.service.IRegistrationOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.hospital.system.service.IScheduleService;
import com.cloud.hospital.system.vo.OrderResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResultVO bookOrder(BookOrderDTO bookOrderDTO) {
        log.info(">>> 收到下单请求: 患者ID={}, 排班ID={}", bookOrderDTO.getPatientId(), bookOrderDTO.getScheduleId());

        // 1. 先查询排班是否存在
        Schedule schedule = scheduleService.getById(bookOrderDTO.getScheduleId());
        if (schedule == null) {
            log.warn("下单失败: 排班记录不存在, ID={}", bookOrderDTO.getScheduleId());
            throw new RuntimeException("该排班不存在");
        }

        // 2. 核心：乐观锁扣减库存
        // UPDATE schedule SET available_num = available_num - 1, version = version + 1
        // WHERE id = ? AND version = ? AND available_num > 0
        LambdaUpdateWrapper<Schedule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.setSql("available_num = available_num - 1, version = version + 1")
                .eq(Schedule::getId, bookOrderDTO.getScheduleId())
                .eq(Schedule::getVersion, schedule.getVersion()) // 乐观锁：版本号匹配
                .gt(Schedule::getAvailableNum, 0);               // 兜底：余号必须大于0

        boolean updateSuccess = scheduleService.update(updateWrapper);

        if (!updateSuccess) {
            log.error("抢号失败: 触发高并发冲突或号源已抢光, 排班ID={}", bookOrderDTO.getScheduleId());
            throw new BusinessException(ExceptionCode.SOURCE_NOT_ENOUGH,"号抢没了,换一个号源试试");
        }


        log.info("库存扣减成功，开始生成订单...");
        // 3. 生成全局唯一的订单号 (此处预留给即将编写的雪花算法)
        long snowflakeId = idGenerator.nextId();
        String orderNo = "ORD" + snowflakeId;
         log.info(">>> 订单号生成成功: {}", orderNo);

        // 4. 构建并保存订单实体 (使用枚举类 OrderStatusEnum)
//        RegistrationOrder order = new RegistrationOrder();
//        order.setId(snowflakeId);                    // 主键ID (DDL要求雪花算法生成)
//        order.setOrderNo(orderNo);                  // 业务单号
//        order.setPatientId(bookOrderDTO.getPatientId());
//        order.setScheduleId(schedule.getId());      // 排班ID
//
//        // 从 schedule 实体中平移过来的关键字段，解决数据库非空报错
//        order.setDoctorId(schedule.getDoctorId());  // 医生ID
//        order.setDepartmentId(schedule.getDepartmentId()); // 科室ID
//
//        // 设置就诊时间 (通常由排班日期 + 班次决定，这里先简单取 work_date)
//        // 注意：DDL 是 datetime 类型，需要转换一下
//        order.setReserveTime(schedule.getWorkDate().atStartOfDay());
//
//        order.setPayAmount(schedule.getAmount());    // 实际金额
//        order.setStatus(OrderStatusEnum.WAITING_PAY); // 默认待支付
//        // ----------------
//
//        order.setCreateTime(LocalDateTime.now());
        RegistrationOrder order = RegistrationOrder.builder()
                .orderNo(orderNo)
                .id(snowflakeId)
                .patientId(bookOrderDTO.getPatientId())
                .scheduleId(schedule.getId())
                .departmentId(schedule.getDepartmentId())
                .doctorId(schedule.getDoctorId())
                .reserveTime(schedule.getWorkDate().atStartOfDay())
                .status(OrderStatusEnum.WAITING_PAY)
                .payAmount(schedule.getAmount())
                .createTime(LocalDateTime.now())
                .build();
        this.save(order);
        log.info(">>> [下单成功] 订单已入库: {}", orderNo);


        // 5. 使用静态工厂或 Builder 模式返回 VO
        return OrderResultVO.builder()
                .orderNo(order.getOrderNo())
                .payAmount(order.getPayAmount())
                .status(order.getStatus().getCode()) // 返回枚举对应的数值 0
                .build();
    }
}
