package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationLogQuery extends BaseParma {
    private String operModule;
    private String operType;
    private String operUserName;
}
