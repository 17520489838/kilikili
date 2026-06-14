package com.kilikili.mappers;

import com.kilikili.entity.po.VideoP;
import com.kilikili.entity.query.VideoPQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoPMapper {
    Integer insert(VideoP videoP);
    Integer insertBatch(@Param("list") List<VideoP> list);
    Integer deleteByPId(@Param("pId") String pId);
    Integer deleteByVideoId(@Param("videoId") String videoId);
    Integer updateByPId(VideoP videoP);
    VideoP selectByPId(@Param("pId") String pId);
    List<VideoP> selectListByCondition(@Param("query") VideoPQuery query);
    Long selectCountByCondition(@Param("query") VideoPQuery query);
}
