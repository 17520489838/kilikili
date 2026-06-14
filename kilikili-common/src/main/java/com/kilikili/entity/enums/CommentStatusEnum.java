package com.kilikili.entity.enums;

/**
 * 评论状态枚举
 */
public enum CommentStatusEnum {
    AUDITING(0, "待审核"),
    AUDIT_PASS(1, "已审核"),
    DELETED(2, "已删除");

    private final Integer code;
    private final String desc;

    CommentStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CommentStatusEnum getByCode(Integer code) {
        for (CommentStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
