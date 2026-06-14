package com.kilikili.mappers;

import com.kilikili.entity.po.UserFocus;
import com.kilikili.entity.query.UserFocusQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFocusMapper {
    Integer insert(UserFocus userFocus);
    Integer deleteByUserFocus(@Param("userId") String userId, @Param("focusUserId") String focusUserId);
    UserFocus selectByUserFocus(@Param("userId") String userId, @Param("focusUserId") String focusUserId);
    List<UserFocus> selectListByCondition(@Param("query") UserFocusQuery query);
    Long selectCountByCondition(@Param("query") UserFocusQuery query);
    Long selectFansCount(@Param("userId") String userId);
    Long selectFocusCount(@Param("userId") String userId);
}
