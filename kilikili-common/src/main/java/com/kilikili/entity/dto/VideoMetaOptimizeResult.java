package com.kilikili.entity.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class VideoMetaOptimizeResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String optimizedTitle;
    private List<String> tags;
    private String optimizedDescription;
}
