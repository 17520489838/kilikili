package com.kilikili.exception;

import com.kilikili.entity.enums.ResponseCodeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {
    private Integer code;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResponseCodeEnum.BUSINESS_ERROR.getCode();
        this.message = message;
    }

    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getMessage());
        this.code = responseCodeEnum.getCode();
        this.message = responseCodeEnum.getMessage();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResponseCodeEnum.BUSINESS_ERROR.getCode();
        this.message = message;
    }
    public BusinessException(ResponseCodeEnum responseCodeEnum, Throwable cause) {
        super(responseCodeEnum.getMessage(), cause);
        this.code = responseCodeEnum.getCode();
        this.message = responseCodeEnum.getMessage();
    }
}
