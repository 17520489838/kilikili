package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserActionQuery extends BaseParma {
    private String userId;
    private String videoId;
    private Integer actionType;
    private String commentId;
}
