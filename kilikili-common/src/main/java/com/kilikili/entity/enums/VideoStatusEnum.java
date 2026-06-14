package com.kilikili.entity.enums;

/**
 * 视频状态枚举
 */
public enum VideoStatusEnum {
    AUDITING(0, "待审核"),
    AUDIT_PASS(1, "审核通过"),
    AUDIT_FAIL(2, "审核不通过"),
    PUBLISHED(3, "已发布");

    private final Integer code;
    private final String desc;

    VideoStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static VideoStatusEnum getByCode(Integer code) {
        for (VideoStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
