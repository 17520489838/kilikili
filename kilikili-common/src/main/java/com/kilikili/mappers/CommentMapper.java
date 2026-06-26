package com.kilikili.mappers;

import com.kilikili.entity.po.Comment;
import com.kilikili.entity.query.CommentQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    Integer insert(Comment comment);
    Integer updateLikeCount(@Param("commentId") String commentId, @Param("delta") Integer delta);
    Integer insertBatch(@Param("list") List<Comment> list);
    Integer deleteByCommentId(@Param("commentId") String commentId);
    Integer deleteByVideoId(@Param("videoId") String videoId);
    Integer updateByCommentId(Comment comment);
    Comment selectByCommentId(@Param("commentId") String commentId);
    List<Comment> selectListByCondition(@Param("query") CommentQuery query);
    Long selectCountByCondition(@Param("query") CommentQuery query);
}
