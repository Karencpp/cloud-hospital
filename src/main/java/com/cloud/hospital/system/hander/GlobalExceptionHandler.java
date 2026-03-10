package com.cloud.hospital.system.hander;

import com.cloud.hospital.system.MyException.BusinessException;
import com.cloud.hospital.system.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常拦截器：把所有的报错都拦截并转换成 Result 格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 拦截业务异常 (RuntimeException)
     * 我们刚才在 Service 里抛出的 "号源已抢空" 就会被这里抓住
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntimeException(RuntimeException e) {
        log.error("检测到业务异常: ", e);
        // 返回 500 错误码和具体的报错信息
        return Result.error(500, "发生错误");
    }

    /**
     * 2. 拦截系统兜底异常 (Exception)
     * 处理那些没预料到的空指针、数据库断开等致命错误
     */
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        log.error("检测到系统运行时异常: ", e);
        return Result.error(500, "服务器开小差了，请稍后再试");
    }
    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        log.warn("业务逻辑警告: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
}