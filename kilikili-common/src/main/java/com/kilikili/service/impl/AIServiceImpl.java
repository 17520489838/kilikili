package com.kilikili.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kilikili.entity.dto.VideoMetaOptimizeResult;
import com.kilikili.service.AIService;
import com.kilikili.service.PromptTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("aiService")
public class AIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    @Resource
    @Qualifier("aiRestTemplate")
    private RestTemplate restTemplate;

    @Resource
    private PromptTemplateService promptTemplateService;

    @Value("${ai.llm.endpoint}")
    private String endpoint;

    @Value("${ai.llm.api-key:}")
    private String apiKey;

    @Value("${ai.llm.model:gpt-3.5-turbo}")
    private String model;

    @Override
    public Integer auditText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 1;
        }
        try {
            String prompt = promptTemplateService.buildAuditPrompt(text.trim());
            String response = callLLM(prompt);
            if (response == null) {
                return 0;
            }
            int result = Integer.parseInt(response.trim());
            if (result < 0 || result > 3) {
                return 0;
            }
            return result;
        } catch (Throwable t) {
            logger.warn("AI审核调用失败，保持默认未审核状态", t);
            return 0;
        }
    }

    @Override
    public VideoMetaOptimizeResult optimizeVideoMeta(String title, String description) {
        String prompt = promptTemplateService.buildOptimizePrompt(title, description);
        String response = callLLM(prompt);

        // Parse JSON response
        JSONObject json = JSON.parseObject(response);
        VideoMetaOptimizeResult result = new VideoMetaOptimizeResult();
        result.setOptimizedTitle(json.getString("optimizedTitle"));
        result.setOptimizedDescription(json.getString("optimizedDescription"));

        // tags may be JSONArray or comma-separated string
        Object tagsObj = json.get("tags");
        if (tagsObj instanceof List) {
            result.setTags((List<String>) tagsObj);
        } else if (tagsObj instanceof String) {
            result.setTags(Arrays.asList(((String) tagsObj).split("[,，]")));
        }

        return result;
    }

    private String callLLM(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (apiKey != null && !apiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + apiKey);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("temperature", 0.1);

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个专业的内容审核与优化助手。请严格按照指令返回结果，不要附加任何解释。");

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", prompt);

        body.put("messages", Arrays.asList(systemMsg, userMsg));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        String responseJson = restTemplate.postForObject(endpoint, request, String.class);
        JSONObject responseObj = JSON.parseObject(responseJson);

        // Extract content from OpenAI-compatible response format
        return responseObj.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
    }
}
