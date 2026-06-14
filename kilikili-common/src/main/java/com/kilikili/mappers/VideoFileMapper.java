package com.kilikili.mappers;

import com.kilikili.entity.po.VideoFile;
import com.kilikili.entity.query.VideoFileQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoFileMapper {
    Integer insert(VideoFile videoFile);
    Integer insertBatch(@Param("list") List<VideoFile> list);
    Integer deleteByFileId(@Param("fileId") String fileId);
    Integer updateByFileId(VideoFile videoFile);
    VideoFile selectByFileId(@Param("fileId") String fileId);
    VideoFile selectByUploadId(@Param("uploadId") String uploadId);
    List<VideoFile> selectListByCondition(@Param("query") VideoFileQuery query);
    Long selectCountByCondition(@Param("query") VideoFileQuery query);
}
