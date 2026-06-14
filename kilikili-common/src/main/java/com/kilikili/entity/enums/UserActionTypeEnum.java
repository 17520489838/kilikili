package com.kilikili.entity.enums;

/**
 * 用户行为类型枚举
 */
public enum UserActionTypeEnum {
    LIKE(0, "点赞"),
    COLLECT(1, "收藏"),
    COIN(2, "投币"),
    SHARE(3, "分享");

    private final Integer code;
    private final String desc;

    UserActionTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static UserActionTypeEnum getByCode(Integer code) {
        for (UserActionTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
