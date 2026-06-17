package com.kilikili.entity.enums;

/**
 * 消息类型枚举
 */
public enum MessageTypeEnum {
    SYSTEM(0, "系统通知"),
    COMMENT(1, "评论"),
    LIKE(2, "点赞"),
    COLLECT(3, "收藏"),
    FOCUS(4, "关注"),
    COIN(5, "投币");

    private final Integer code;
    private final String desc;

    MessageTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageTypeEnum getByCode(Integer code) {
        for (MessageTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
