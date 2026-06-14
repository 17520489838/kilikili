package com.kilikili.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频信息表
 */
@Data
public class Video implements Serializable {
    private Long id;
    private String videoId;
    private String userId;
    
    // 用户名（非数据库字段，用于展示上传者昵称）
    private String userName;
    private String videoName;
    private String videoCover;
    private Integer categoryId;
    private Integer pCategoryId;
    private Integer postType;
    private String tags;
    private String introduction;
    private String interaction;
    private Integer duration;
    private Integer playCount;
    private Integer likeCount;
    private Integer coinCount;
    private Integer collectCount;
    private Integer commentCount;
    private Integer danmuCount;
    private Integer shareCount;
    private Integer status;
    private Integer recommendType;
    private String auditReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastPlayTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Integer isDeleted;
}
