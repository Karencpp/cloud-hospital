package com.cloud.hospital.system.common;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    /**
     * 0-待支付
     */
    WAITING_PAY(0, "待支付"),

    /**
     * 1-已支付
     */
    PAID(1, "已支付"),

    /**
     * 2-已取消
     */
    CANCELLED(2, "已取消");

    // 💡 关键点 1：@EnumValue 告诉 MyBatis-Plus 存入数据库的是这个 code 字段
    @EnumValue
    private final int code;

    // 💡 关键点 2：@JsonValue 告诉 Spring (Jackson) 返回给前端 JSON 时展示这个描述，
    // 或者你可以删掉它，让前端也拿 code。
    @JsonValue
    private final String description;
}
