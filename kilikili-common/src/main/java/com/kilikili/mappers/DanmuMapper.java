package com.kilikili.mappers;

import com.kilikili.entity.po.Danmu;
import com.kilikili.entity.query.DanmuQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DanmuMapper {
    Integer insert(Danmu danmu);
    Integer insertBatch(@Param("list") List<Danmu> list);
    Integer deleteByDanmuId(@Param("danmuId") String danmuId);
    Integer deleteByVideoId(@Param("videoId") String videoId);
    Integer updateByDanmuId(Danmu danmu);
    Danmu selectByDanmuId(@Param("danmuId") String danmuId);
    List<Danmu> selectListByCondition(@Param("query") DanmuQuery query);
    Long selectCountByCondition(@Param("query") DanmuQuery query);
}
