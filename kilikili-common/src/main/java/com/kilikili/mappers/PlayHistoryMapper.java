package com.kilikili.mappers;

import com.kilikili.entity.po.PlayHistory;
import com.kilikili.entity.query.PlayHistoryQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlayHistoryMapper {
    Integer insert(PlayHistory playHistory);
    Integer insertBatch(@Param("list") List<PlayHistory> list);
    Integer deleteByUserIdVideoId(@Param("userId") String userId, @Param("videoId") String videoId);
    Integer deleteByUserId(@Param("userId") String userId);
    Integer updateByUserIdVideoId(PlayHistory playHistory);
    PlayHistory selectByUserIdVideoId(@Param("userId") String userId, @Param("videoId") String videoId);
    List<PlayHistory> selectListByCondition(@Param("query") PlayHistoryQuery query);
    Long selectCountByCondition(@Param("query") PlayHistoryQuery query);
}
