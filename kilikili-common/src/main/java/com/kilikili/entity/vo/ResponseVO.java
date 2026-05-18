package com.kilikili.entity.vo;

import lombok.Data;

@Data
public class ResponseVO {
    private Integer code;
    private String message;
    private Object data;



    public ResponseVO(Integer code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
