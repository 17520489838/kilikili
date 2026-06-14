package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserFocusQuery extends BaseParma {
    private String userId;
    private String focusUserId;
}
