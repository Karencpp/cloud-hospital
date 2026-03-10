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
 * 医院科室表
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Getter
@Setter
@TableName("department")
@Schema(name = "Department", description = "医院科室表")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "科室ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "科室名称(如: 心内科)")
    @TableField("name")
    private String name;

    @Schema(description = "科室编码")
    @TableField("code")
    private String code;

    @Schema(description = "科室描述")
    @TableField("description")
    private String description;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
