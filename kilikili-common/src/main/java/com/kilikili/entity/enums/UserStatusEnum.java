package com.kilikili.entity.enums;

/**
 * 用户状态枚举
 */
public enum UserStatusEnum {
    DISABLE(0, "禁用"),
    NORMAL(1, "正常"),
    SECRECY(2, "保密"),
    CANCELED( 3, "注销");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
