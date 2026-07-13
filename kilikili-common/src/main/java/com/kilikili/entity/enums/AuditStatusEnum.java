package com.kilikili.entity.enums;

/**
 * AI审核状态枚举
 */
public enum AuditStatusEnum {
    UNREVIEWED(0, "未审核"),
    PASSED(1, "通过"),
    SUSPICIOUS(2, "可疑"),
    VIOLATION(3, "违规");

    private final Integer code;
    private final String desc;

    AuditStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static AuditStatusEnum getByCode(Integer code) {
        for (AuditStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
