package com.kilikili.service;

/**
 * 视频转码服务 - 异步将上传的视频转码为浏览器可播放格式
 */
public interface VideoTranscodeService {

    /**
     * 开始转码任务
     * @param fileId 视频文件ID
     * @param inputPath 原始视频文件路径
     */
    void startTranscode(String fileId, String inputPath);
}
