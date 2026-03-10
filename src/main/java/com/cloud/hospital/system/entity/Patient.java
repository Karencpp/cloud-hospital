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
 * 患者基础信息表
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Getter
@Setter
@TableName("patient")
@Schema(name = "Patient", description = "患者基础信息表")
public class Patient implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "患者姓名")
    @TableField("name")
    private String name;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "身份证号(实际大厂会要求脱敏或加密存储)")
    @TableField("id_card_no")
    private String idCardNo;

    @Schema(description = "性别：0-未知, 1-男, 2-女")
    @TableField("gender")
    private Byte gender;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @Schema(description = "逻辑删除标识：0-未删除, 1-已删除")
    @TableField("is_deleted")
    @TableLogic
    private Byte isDeleted;
}
