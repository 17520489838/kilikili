package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysSettingQuery extends BaseParma {
    private String settingKey;
}
