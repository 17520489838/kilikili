package com.kilikili.admin.controller;

import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;


/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice// AOP
public class AGlobalExceptionHandlerController {
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseVO handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error("业务异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return new ResponseVO(e.getCode(), e.getMessage(), null);
    }

    /**
     * 处理参数校验异常 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseVO handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("参数校验异常: {}", request.getRequestURI(), e);
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数错误";
        return new ResponseVO(ResponseCodeEnum.PARAM_ERROR.getCode(), message, null);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseVO handleBindException(BindException e, HttpServletRequest request) {
        log.error("参数绑定异常: {}", request.getRequestURI(), e);
        FieldError fieldError = e.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数错误";
        return new ResponseVO(ResponseCodeEnum.PARAM_ERROR.getCode(), message, null);
    }

    /**
     * 处理约束违反异常 (@Validated)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseVO handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        log.error("参数校验异常: {}", request.getRequestURI(), e);
        ConstraintViolation<?> constraintViolation = e.getConstraintViolations().stream().findFirst().orElse(null);
        String message = constraintViolation != null ? constraintViolation.getMessage() : "参数错误";
        return new ResponseVO(ResponseCodeEnum.PARAM_ERROR.getCode(), message, null);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseVO handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("非法参数异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return new ResponseVO(ResponseCodeEnum.PARAM_ERROR.getCode(), e.getMessage(), null);
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseVO handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常: {}", request.getRequestURI(), e);
        return new ResponseVO(ResponseCodeEnum.ERROR.getCode(), "系统内部错误", null);
    }

    /**
     * 处理其他所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseVO handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return new ResponseVO(ResponseCodeEnum.ERROR.getCode(), "服务器内部错误", null);
    }
}
