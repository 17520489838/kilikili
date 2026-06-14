package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoQuery extends BaseParma {
    private String videoId;
    private String userId;
    private String videoNameFuzzy;
    private Integer categoryId;
    private Integer pCategoryId;
    private Integer status;
    private Integer recommendType;
    private String tags;
    private String introductionFuzzy;
    private Date createTimeStart;
    private Date createTimeEnd;
    private String keyword;
}
