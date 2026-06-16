package com.kilikili.web.config;

import com.kilikili.utils.FFmpegUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

@Component
public class FfmpegConfig {

    private static final Logger logger = LoggerFactory.getLogger(FfmpegConfig.class);

    @Value("${ffmpeg.path:}")
    private String ffmpegPath;

    @PostConstruct
    public void init() {
        String path = ffmpegPath;
        if (path == null || path.isEmpty()) {
            // Try to find ffmpeg in PATH
            path = "ffmpeg";
        } else {
            File f = new File(path);
            if (!f.exists()) {
                logger.warn("配置的FFmpeg路径不存在: {}, 将使用系统PATH中的ffmpeg", path);
                path = "ffmpeg";
            }
        }
        FFmpegUtils.setFfmpegPath(path);
        logger.info("FFmpeg路径设置为: {}", path);
    }
}
