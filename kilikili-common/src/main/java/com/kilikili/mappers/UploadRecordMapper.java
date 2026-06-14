package com.kilikili.mappers;

import com.kilikili.entity.po.UploadRecord;
import com.kilikili.entity.query.UploadRecordQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UploadRecordMapper {
    Integer insert(UploadRecord uploadRecord);
    Integer updateByUploadId(UploadRecord uploadRecord);
    UploadRecord selectByUploadId(@Param("uploadId") String uploadId);
    List<UploadRecord> selectListByCondition(@Param("query") UploadRecordQuery query);
    Long selectCountByCondition(@Param("query") UploadRecordQuery query);
}
