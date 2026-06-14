package com.kilikili.entity.enums;

/**
 * 文件状态枚举
 */
public enum FileStatusEnum {
    UPLOADING(0, "上传中"),
    UPLOAD_FINISH(1, "上传完成"),
    TRANSCODING(2, "转码中"),
    TRANSCODE_FINISH(3, "转码完成"),
    TRANSCODE_FAIL(4, "转码失败");

    private final Integer code;
    private final String desc;

    FileStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FileStatusEnum getByCode(Integer code) {
        for (FileStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
