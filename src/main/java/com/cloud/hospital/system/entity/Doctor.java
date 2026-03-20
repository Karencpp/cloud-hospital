package com.cloud.hospital.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 医生信息表
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Getter
@Setter
@TableName("doctor")
@Schema(name = "Doctor", description = "医生信息表")
public class Doctor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "医生ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "医生姓名")
    @TableField("name")
    private String name;

    @Schema(description = "所属科室ID")
    @TableField("department_id")
    private Long departmentId;

    @Schema(description = "职称(如: 主任医师, 主治医师)")
    @TableField("title")
    private String title;

    @Schema(description = "擅长领域")
    @TableField("specialty")
    private String specialty;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;  //0代表没有删除, 1代表已删除
}
