package com.cloud.hospital.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 医生排班与号源表
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Getter
@Setter
@TableName("schedule")
@Schema(name = "Schedule", description = "医生排班与号源表")
public class Schedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "排班ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "医生ID")
    @TableField("doctor_id")
    private Long doctorId;

    @Schema(description = "科室ID")
    @TableField("department_id")
    private Long departmentId;

    @Schema(description = "出诊日期")
    @TableField("work_date")
    private LocalDate workDate;

    @Schema(description = "班次：1-上午, 2-下午, 3-全天")
    @TableField("shift_type")
    private Byte shiftType;

    @Schema(description = "总号源数")
    @TableField("total_num")
    private Integer totalNum;

    @Schema(description = "剩余可用号源数")
    @TableField("available_num")
    private Integer availableNum;

    @Schema(description = "挂号费(保留两位小数)")
    @TableField("amount")
    private BigDecimal amount;

    @Schema(description = "乐观锁版本号(防超卖核心)")
    @TableField("version")
    private Integer version;

    @Schema(description = "状态：0-停诊, 1-正常出诊")
    @TableField("status")
    private Byte status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
