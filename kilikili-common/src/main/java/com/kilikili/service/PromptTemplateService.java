package com.kilikili.service;

import org.springframework.stereotype.Service;

/**
 * Prompt模板构建，仅传文本内容，绝不包含用户PII
 */
@Service("promptTemplateService")
public class PromptTemplateService {

    /**
     * 构建内容审核Prompt，仅传入待审核文本
     */
    public String buildAuditPrompt(String text) {
        return "你是一个内容安全审核助手。请审核以下用户发布的文本内容，判断其是否包含违规信息（色情、暴力、仇恨言论、广告 spam、诈骗等）。\n" +
                "\n" +
                "审核规则：\n" +
                "- 如果内容正常、安全，返回 1\n" +
                "- 如果内容存在轻微可疑（如边界模糊的调侃），返回 2\n" +
                "- 如果内容明确违规（如人身攻击、色情、违法广告），返回 3\n" +
                "\n" +
                "请严格只返回一个数字（1、2 或 3），不要返回其他任何内容。\n" +
                "\n" +
                "待审核文本：\n" +
                text;
    }

    /**
     * 构建视频元数据优化Prompt，仅传入标题和简介
     */
    public String buildOptimizePrompt(String title, String description) {
        return "你是一个视频内容运营专家。请根据用户提供的视频标题和简介，帮助优化视频元数据，使其更具吸引力和搜索友好性。\n" +
                "\n" +
                "要求：\n" +
                "1. optimizedTitle：优化后的标题（20字以内，吸引眼球但不过分标题党）\n" +
                "2. tags：生成3-5个精准标签（数组形式），用于视频分类和搜索\n" +
                "3. optimizedDescription：润色后的简介（100字以内），突出视频亮点\n" +
                "\n" +
                "请严格按以下JSON格式返回（不要包含markdown代码块标记）：\n" +
                "{\"optimizedTitle\":\"...\",\"tags\":[\"标签1\",\"标签2\"],\"optimizedDescription\":\"...\"}\n" +
                "\n" +
                "原始标题：" + title + "\n" +
                "原始简介：" + (description != null && !description.isEmpty() ? description : "（无）");
    }
}
