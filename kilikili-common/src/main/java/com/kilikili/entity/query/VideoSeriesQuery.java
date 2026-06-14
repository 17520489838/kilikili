package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoSeriesQuery extends BaseParma {
    private String seriesId;
    private String userId;
    private String seriesNameFuzzy;
}
