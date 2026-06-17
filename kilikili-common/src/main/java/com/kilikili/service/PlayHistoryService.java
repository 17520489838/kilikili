package com.kilikili.service;

import com.kilikili.entity.vo.PaginationResultVO;

import java.util.Map;

public interface PlayHistoryService {
    PaginationResultVO<Map<String, Object>> loadHistory(String userId, Integer pageNo);
    void delHistory(String userId, String videoId);
    void cleanHistory(String userId);
    void saveHistory(String userId, String videoId, String fileId, Integer progress, Integer duration);
}
