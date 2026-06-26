package com.kilikili.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentQuery extends BaseParma {
    private String commentId;
    private String videoId;
    private String userId;
    private String pCommentId;
    private Boolean pCommentIdNull; // Filter for p_comment_id IS NULL
    private Integer status;
    private Integer topType;
    private Date createTimeStart;
    private Date createTimeEnd;
}
