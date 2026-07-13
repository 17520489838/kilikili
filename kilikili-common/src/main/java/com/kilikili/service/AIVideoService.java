package com.kilikili.service;

import com.kilikili.entity.dto.VideoMetaOptimizeResult;

public interface AIVideoService {

    /**
     * 优化视频元数据（纯读操作，无DB写），可能因LLM失败抛异常
     */
    VideoMetaOptimizeResult optimizeVideoMeta(String title, String description);
}
