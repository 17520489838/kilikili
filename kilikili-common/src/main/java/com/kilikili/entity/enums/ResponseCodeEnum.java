package com.kilikili.entity.enums;

public enum ResponseCodeEnum {
    SUCCESS(200, "请求成功"),
    ERROR(500, "服务器内部错误"),
    BUSINESS_ERROR(400, "业务异常"),
    PARAM_ERROR(401, "参数错误"),
    UNAUTHORIZED(403, "未授权"),
    NOT_FOUND(404, "资源不存在"),
    //邮箱已存在
    EMAIL_EXIST(405, "邮箱已存在"),
    NICK_NAME_EXIST(406, "昵称已存在"),
    USER_EXIST(408, "用户已存在"),
    USER_DISABLED(409, "用户已禁用"),
    USER_NOT_EXIST_OR_PASSWORD_ERROR( 412, "用户不存在或密码错误"),
    USER_CANCELED( 413, "用户已注销");




    private final Integer code;
    private final String message;

    ResponseCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
