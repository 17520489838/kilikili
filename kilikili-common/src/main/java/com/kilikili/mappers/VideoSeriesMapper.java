package com.kilikili.mappers;

import com.kilikili.entity.po.VideoSeries;
import com.kilikili.entity.query.VideoSeriesQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoSeriesMapper {
    Integer insert(VideoSeries videoSeries);
    Integer insertBatch(@Param("list") List<VideoSeries> list);
    Integer deleteBySeriesId(@Param("seriesId") String seriesId);
    Integer updateBySeriesId(VideoSeries videoSeries);
    VideoSeries selectBySeriesId(@Param("seriesId") String seriesId);
    List<VideoSeries> selectListByCondition(@Param("query") VideoSeriesQuery query);
    Long selectCountByCondition(@Param("query") VideoSeriesQuery query);
}
