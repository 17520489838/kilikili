package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DanmuQuery extends BaseParma {
    private String danmuId;
    private String videoId;
    private String fileId;
    private String userId;
    private Integer timePointStart;
    private Integer timePointEnd;
    private String textFuzzy;
}
