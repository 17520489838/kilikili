package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageQuery extends BaseParma {
    private String messageId;
    private String receiveUserId;
    private String sendUserId;
    private Integer messageType;
    private Integer isRead;
    private Date createTimeStart;
    private Date createTimeEnd;
}
