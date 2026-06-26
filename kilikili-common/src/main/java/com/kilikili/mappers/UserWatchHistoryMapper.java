package com.kilikili.mappers;

import com.kilikili.entity.po.UserWatchHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface UserWatchHistoryMapper {
    Integer insertOrUpdate(@Param("userId") String userId, @Param("videoId") String videoId);

    List<UserWatchHistory> selectRecentByUserId(@Param("userId") String userId, @Param("limit") Integer limit);

    List<UserWatchHistory> selectRecentByUserIdOffset(@Param("userId") String userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    Long countByUserId(@Param("userId") String userId);

    List<UserWatchHistory> selectOldByUserId(@Param("userId") String userId, @Param("offset") Integer offset);

    Integer deleteByUserIdVideoId(@Param("userId") String userId, @Param("videoId") String videoId);

    Integer deleteByUserId(@Param("userId") String userId);

    List<String> selectDistinctUserIds();

    Long selectCountByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    List<UserWatchHistory> selectListByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
