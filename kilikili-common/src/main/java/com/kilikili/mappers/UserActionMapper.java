package com.kilikili.mappers;

import com.kilikili.entity.po.UserAction;
import com.kilikili.entity.query.UserActionQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserActionMapper {
    Integer insert(UserAction userAction);
    Integer insertBatch(@Param("list") List<UserAction> list);
    Integer updateByUserVideoAction(UserAction userAction);
    UserAction selectByUserVideoAction(@Param("userId") String userId, @Param("videoId") String videoId, @Param("actionType") Integer actionType);
    UserAction selectByUserCommentAction(@Param("userId") String userId, @Param("commentId") String commentId, @Param("actionType") Integer actionType);
    List<UserAction> selectListByCondition(@Param("query") UserActionQuery query);
    Long selectCountByCondition(@Param("query") UserActionQuery query);
}
