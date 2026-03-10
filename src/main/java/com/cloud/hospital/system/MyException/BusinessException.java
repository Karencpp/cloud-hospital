package com.cloud.hospital.system.MyException;

import com.cloud.hospital.system.constant.ExceptionCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = ExceptionCode.SOURCE_NOT_ENOUGH;
    }
}