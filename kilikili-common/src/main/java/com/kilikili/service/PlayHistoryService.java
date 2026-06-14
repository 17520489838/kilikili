package com.kilikili.service;

import com.kilikili.entity.po.PlayHistory;
import com.kilikili.entity.vo.PaginationResultVO;

public interface PlayHistoryService {
    PaginationResultVO<PlayHistory> loadHistory(String userId, Integer pageNo);
    void delHistory(String userId, String videoId);
    void cleanHistory(String userId);
    void saveHistory(String userId, String videoId, String fileId, Integer progress, Integer duration);
}
