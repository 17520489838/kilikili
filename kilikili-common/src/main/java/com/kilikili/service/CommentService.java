package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.Comment;
import com.kilikili.entity.query.CommentQuery;
import com.kilikili.entity.vo.PaginationResultVO;

public interface CommentService {
    void postComment(TokenUserInfoDto token, String videoId, String content, String replyCommentId, String imgPath);
    PaginationResultVO<Comment> loadComment(String videoId, Integer pageNo, Integer orderType);
    void topComment(String commentId, String userId);
    void cancelTopComment(String commentId, String userId);
    void userDelComment(String commentId, String userId);
    PaginationResultVO<Comment> loadCommentPage(CommentQuery query);
    void delCommentByAdmin(String commentId);
}
