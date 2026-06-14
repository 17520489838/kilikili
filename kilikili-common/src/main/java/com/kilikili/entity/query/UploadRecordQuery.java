package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UploadRecordQuery extends BaseParma {
    private String uploadId;
    private String userId;
    private String fileNameFuzzy;
    private Integer status;
}
