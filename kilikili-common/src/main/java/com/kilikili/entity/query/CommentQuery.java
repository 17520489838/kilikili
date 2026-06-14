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
    private Integer status;
    private Integer topType;
    private Date createTimeStart;
    private Date createTimeEnd;
}
