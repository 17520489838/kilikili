package com.kilikili.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论表
 */
@Data
public class Comment implements Serializable {
    private Long id;
    private String commentId;
    private String videoId;
    private String userId;
    private String content;
    private String imgPath;

    /** Frontend alias: replyCommentId */
    @JsonProperty("replyCommentId")
    public String getPCommentId() {
        return pCommentId;
    }

    private String pCommentId;

    private String replyUserId;
    private Integer likeCount;
    private Integer topType;
    private Integer status;

    /** Frontend alias: postTime */
    @JsonProperty("postTime")
    public Date getCreateTime() {
        return createTime;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    private Integer isDeleted;

    // Transient fields (not in DB)
    private String nickName;
    private String avatar;
}
