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

    @javax.annotation.Resource
    private com.kilikili.mappers.UserWatchHistoryMapper userWatchHistoryMapper;

    @javax.annotation.Resource
    private com.kilikili.service.UserWatchHistoryService userWatchHistoryService;

    /**
     * 每天凌晨清理过期的分片上传临时文件
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanTempFiles() {
        logger.info("开始清理临时文件...");
        File uploadDir = new File(System.getProperty("java.io.tmpdir"), "kilikili/upload");
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] uploadDirs = uploadDir.listFiles();
            if (uploadDirs != null) {
                for (File dir : uploadDirs) {
                    if (dir.isDirectory() && System.currentTimeMillis() - dir.lastModified() > 24 * 60 * 60 * 1000) {
                        deleteDir(dir);
                        logger.info("已删除过期上传临时目录: {}", dir.getName());
                    }
                }
            }
        }
        logger.info("临时文件清理完成");
    }

    /**
     * 每天凌晨3点清理观看历史，每用户保留50条
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanWatchHistory() {
        logger.info("开始清理观看历史...");
        try {
            java.util.List<String> userIds = userWatchHistoryMapper.selectDistinctUserIds();
            for (String uid : userIds) {
                userWatchHistoryService.cleanUpOldRecords(uid, 50);
            }
            logger.info("观看历史清理完成，共处理 {} 个用户", userIds.size());
        } catch (Exception e) {
            logger.error("观看历史清理异常", e);
        }
    }

    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
