package com.kilikili.service;

import com.kilikili.entity.vo.PaginationResultVO;

import java.util.Map;

public interface UserWatchHistoryService {
    void recordWatch(String userId, String videoId);
    PaginationResultVO<Map<String, Object>> loadHistory(String userId, Integer pageNo);
    void delHistory(String userId, String videoId);
    void cleanHistory(String userId);
    void cleanUpOldRecords(String userId, int keepCount);
}
