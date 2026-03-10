package com.cloud.hospital.system.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "全局统一返回结果")
public class Result<T> {

    @Schema(description = "状态码 (200表示成功)")
    private Integer code;

    @Schema(description = "提示信息")
    private String message;

    @Schema(description = "实际返回的数据载体")
    private T data;

    // 成功时的快捷方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 失败时的快捷方法
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}