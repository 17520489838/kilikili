package com.kilikili.service.impl;

import com.kilikili.entity.dto.VideoMetaOptimizeResult;
import com.kilikili.service.AIService;
import com.kilikili.service.AIVideoService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("aiVideoService")
public class AIVideoServiceImpl implements AIVideoService {

    @Resource
    private AIService aiService;

    @Override
    public VideoMetaOptimizeResult optimizeVideoMeta(String title, String description) {
        return aiService.optimizeVideoMeta(title, description);
    }
}
