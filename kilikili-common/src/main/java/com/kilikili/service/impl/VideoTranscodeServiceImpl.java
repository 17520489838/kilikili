package com.kilikili.service.impl;

import com.kilikili.config.Appconfig;
import com.kilikili.entity.enums.FileStatusEnum;
import com.kilikili.entity.po.VideoFile;
import com.kilikili.mappers.VideoFileMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.service.VideoTranscodeService;
import com.kilikili.utils.FFmpegUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;

@Service("videoTranscodeService")
public class VideoTranscodeServiceImpl implements VideoTranscodeService {

    private static final Logger logger = LoggerFactory.getLogger(VideoTranscodeServiceImpl.class);

    @Resource
    private VideoFileMapper videoFileMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private Appconfig appconfig;

    @Override
    @Async("taskExecutor")
    public void startTranscode(String fileId, String inputPath) {
        logger.info("===== 开始转码: fileId={}, inputPath={} =====", fileId, inputPath);

        // 重试获取 VideoFile（因调用方事务可能未提交）
        VideoFile videoFile = null;
        for (int i = 0; i < 30; i++) {
            videoFile = videoFileMapper.selectByFileId(fileId);
            if (videoFile != null) break;
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        if (videoFile == null) {
            logger.error("转码失败: 重试30次后VideoFile仍不存在, fileId={}", fileId);
            return;
        }

        // 更新状态为转码中
        videoFile.setStatus(FileStatusEnum.TRANSCODING.getCode());
        videoFileMapper.updateByFileId(videoFile);
        logger.info("状态已更新为 TRANSCODING, fileId={}", fileId);

        File input = new File(inputPath);
        if (!input.exists()) {
            logger.error("转码失败: 源文件不存在, path={}", inputPath);
            videoFile.setStatus(FileStatusEnum.TRANSCODE_FAIL.getCode());
            videoFileMapper.updateByFileId(videoFile);
            return;
        }
        logger.info("源文件存在, size={} bytes", input.length());

        try {
            // 确定项目目录
            String projectFolder = appconfig.getProjectFolder();
            if (projectFolder == null || projectFolder.isEmpty()) {
                projectFolder = System.getProperty("java.io.tmpdir") + "/kilikili/";
            }
            String transcodeDir = projectFolder + "video/transcode/" + fileId + "/";
            new File(transcodeDir).mkdirs();
            logger.info("转码输出目录: {}", transcodeDir);

            // 提取视频时长
            int duration = FFmpegUtils.getVideoDuration(inputPath);
            logger.info("视频时长: {}s, fileId={}", duration, fileId);
            videoFile.setDuration(duration);
            videoFileMapper.updateByFileId(videoFile);

            // 转码为 MP4 (H.264/AAC)
            String mp4Path = transcodeDir + "index.mp4";
            logger.info("开始MP4转码... fileId={}", fileId);
            boolean mp4Success = FFmpegUtils.convertToMp4(inputPath, mp4Path);

            if (!mp4Success) {
                logger.error("MP4转码失败, fileId={}", fileId);
                videoFile.setStatus(FileStatusEnum.TRANSCODE_FAIL.getCode());
                videoFile.setDuration(duration);
                videoFileMapper.updateByFileId(videoFile);
                return;
            }

            // 验证MP4文件
            File mp4File = new File(mp4Path);
            if (!mp4File.exists() || mp4File.length() == 0) {
                logger.error("MP4转码后文件不存在或为空, fileId={}", fileId);
                videoFile.setStatus(FileStatusEnum.TRANSCODE_FAIL.getCode());
                videoFileMapper.updateByFileId(videoFile);
                return;
            }
            logger.info("MP4转码成功, size={} bytes", mp4File.length());

            // 尝试转码为 HLS (m3u8) - 可选
            boolean hlsSuccess = FFmpegUtils.convertToHls(inputPath, transcodeDir, "index");
            if (hlsSuccess) {
                logger.info("HLS转码成功, fileId={}", fileId);
            } else {
                logger.warn("HLS转码失败（不影响MP4播放）, fileId={}", fileId);
            }

            // 更新VideoFile记录
            videoFile.setFilePath(mp4Path);
            videoFile.setDuration(duration);
            videoFile.setStatus(FileStatusEnum.TRANSCODE_FINISH.getCode());
            videoFileMapper.updateByFileId(videoFile);

            // 更新Video表的duration
            com.kilikili.entity.po.Video video = videoMapper.selectByVideoId(fileId);
            if (video != null) {
                video.setDuration(duration);
                videoMapper.updateByVideoId(video);
                logger.info("Video表duration已更新, videoId={}, duration={}s", fileId, duration);
            }

            // 删除原始合并文件
            if (input.delete()) {
                logger.info("原始文件已删除: {}", inputPath);
            }

            logger.info("===== 转码完成: fileId={}, mp4Path={} =====", fileId, mp4Path);

        } catch (Exception e) {
            logger.error("转码异常: fileId=" + fileId + ", inputPath=" + inputPath, e);
            try {
                videoFile.setStatus(FileStatusEnum.TRANSCODE_FAIL.getCode());
                videoFileMapper.updateByFileId(videoFile);
            } catch (Exception ignored) {}
        }
    }
}
