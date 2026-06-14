package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VideoFileQuery extends BaseParma {
    private String fileId;
    private String userId;
    private String fileNameFuzzy;
    private String uploadId;
    private Integer status;
    private String md5;
}
