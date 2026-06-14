package com.kilikili.service;

import com.kilikili.entity.po.Message;
import com.kilikili.entity.vo.PaginationResultVO;

import java.util.List;
import java.util.Map;

public interface MessageService {
    Long getNoReadCount(String receiveUserId);
    PaginationResultVO<Message> loadMessage(String receiveUserId, Integer pageNo);
    void delMessage(String messageId, String userId);
    List<Map<String, Object>> getNoReadCountGroup(String receiveUserId);
    void readAll(String receiveUserId);
    void addMessage(String receiveUserId, String sendUserId, Integer messageType, String content, String relatedId);
}
