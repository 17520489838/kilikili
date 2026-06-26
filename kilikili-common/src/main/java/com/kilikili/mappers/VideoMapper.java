package com.kilikili.mappers;

import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.VideoQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoMapper {
    Integer insert(Video video);
    Integer insertBatch(@Param("list") List<Video> list);
    Integer deleteByVideoId(@Param("videoId") String videoId);
    Integer updateByVideoId(Video video);
    Video selectByVideoId(@Param("videoId") String videoId);
    Video selectByVideoIdIncludeDeleted(@Param("videoId") String videoId);
    List<Video> selectList();
    List<Video> selectListByCondition(@Param("query") VideoQuery query);
    Long selectCountByCondition(@Param("query") VideoQuery query);
    void updateCount(@Param("videoId") String videoId, @Param("field") String field, @Param("count") Integer count);

    Integer selectTotalLikeCount(@Param("userId") String userId);

    Map<String, Object> selectVideoStatsByUserId(@Param("userId") String userId);
}