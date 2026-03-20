package com.cloud.hospital.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.cloud.hospital.system.common.OrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 挂号订单表
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Getter
@Setter
@Builder
@TableName("registration_order")
@Schema(name = "RegistrationOrder", description = "挂号订单表")
public class RegistrationOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID(强制使用雪花算法生成，禁止自增)")
    @TableId("id")
    private Long id;

    @Schema(description = "业务订单号(用于对账和展示)")
    @TableField("order_no")
    private String orderNo;

    @Schema(description = "患者ID")
    @TableField("patient_id")
    private Long patientId;

    @Schema(description = "排班ID")
    @TableField("schedule_id")
    private Long scheduleId;

    @Schema(description = "医生ID")
    @TableField("doctor_id")
    private Long doctorId;

    @Schema(description = "科室ID")
    @TableField("department_id")
    private Long departmentId;

    @Schema(description = "预计就诊时间")
    @TableField("reserve_time")
    private LocalDateTime reserveTime;

    @Schema(description = "订单状态：0-待支付, 1-已支付待就诊, 2-已取消(超时未支付/主动取消), 3-已完成")
    @TableField("status")
    private OrderStatusEnum status;

    @Schema(description = "实际支付金额")
    @TableField("pay_amount")
    private BigDecimal payAmount;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted; //0代表没有删除, 1代表已删除
}
