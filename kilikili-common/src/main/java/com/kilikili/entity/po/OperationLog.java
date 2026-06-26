package com.kilikili.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class OperationLog implements Serializable {
    private Long id;
    private String operId;
    private String operModule;
    private String operType;
    private String operDesc;
    private String requestUrl;
    private String requestParams;
    private String operUserId;
    private String operUserName;
    private String operIp;
    private Integer status;
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
