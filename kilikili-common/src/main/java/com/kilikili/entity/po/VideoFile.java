package com.kilikili.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频文件表
 */
@Data
public class VideoFile implements Serializable {
    private Long id;
    private String fileId;
    private String userId;
    private String fileName;
    private Long fileSize;
    private String filePath;
    private String fileSuffix;
    private String md5;
    private Integer chunkCount;
    private Long chunkSize;
    private String uploadId;
    private Integer status;
    private Integer duration;
    private String videoCover;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Integer isDeleted;
}
