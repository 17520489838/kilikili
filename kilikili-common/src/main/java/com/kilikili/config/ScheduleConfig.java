package com.kilikili.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;

/**
 * 定时任务配置
 */
@Configuration
@EnableScheduling
public class ScheduleConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleConfig.class);

    @javax.annotation.Resource
    private com.kilikili.config.Appconfig appconfig;

    /**
     * 每天凌晨清理过期的上传临时文件
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanTempFiles() {
        logger.info("开始清理临时文件...");
        String projectFolder = appconfig.getProjectFolder();
        if (projectFolder != null && !projectFolder.isEmpty()) {
            File tempDir = new File(projectFolder, "temp");
            if (tempDir.exists() && tempDir.isDirectory()) {
                File[] files = tempDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && System.currentTimeMillis() - file.lastModified() > 24 * 60 * 60 * 1000) {
                            if (file.delete()) {
                                logger.info("已删除临时文件: {}", file.getName());
                            }
                        }
                    }
                }
            }
        }
        logger.info("临时文件清理完成");
    }
}
