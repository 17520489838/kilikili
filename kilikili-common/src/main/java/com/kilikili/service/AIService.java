package com.kilikili.service;

import com.kilikili.entity.dto.VideoMetaOptimizeResult;

public interface AIService {

    /**
     * 审核文本内容，返回audit_status码 (0/1/2/3)，永不抛异常
     */
    Integer auditText(String text);

    /**
     * 优化视频元数据，可能因LLM调用失败抛异常
     */
    VideoMetaOptimizeResult optimizeVideoMeta(String title, String description);
}
