package com.kilikili.mappers;

import com.kilikili.entity.po.Message;
import com.kilikili.entity.query.MessageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    Integer insert(Message message);
    Integer insertBatch(@Param("list") List<Message> list);
    Integer deleteByMessageId(@Param("messageId") String messageId);
    Integer deleteByReceiveUserId(@Param("receiveUserId") String receiveUserId);
    Integer updateByMessageId(Message message);
    Message selectByMessageId(@Param("messageId") String messageId);
    List<Message> selectListByCondition(@Param("query") MessageQuery query);
    Long selectCountByCondition(@Param("query") MessageQuery query);
    Long selectNoReadCount(@Param("receiveUserId") String receiveUserId);
    void readAll(@Param("receiveUserId") String receiveUserId);
}
