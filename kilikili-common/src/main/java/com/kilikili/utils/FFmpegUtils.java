package com.kilikili.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * FFmpeg工具类 - 用于视频转码
 */
public class FFmpegUtils {

    private static final Logger logger = LoggerFactory.getLogger(FFmpegUtils.class);

    private static String ffmpegPath = "ffmpeg";

    public static void setFfmpegPath(String path) {
        ffmpegPath = path;
    }

    /**
     * 将视频转码为HLS(m3u8)格式
     */
    public static boolean convertToHls(String inputPath, String outputDir, String fileName) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String outputPath = outputDir + File.separator + fileName + ".m3u8";
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", inputPath,
                "-profile:v", "baseline",
                "-level", "3.0",
                "-start_number", "0",
                "-hls_time", "10",
                "-hls_list_size", "0",
                "-f", "hls",
                "-c:v", "libx264",
                "-c:a", "aac",
                "-strict", "-2",
                outputPath
        );
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logger.debug("FFmpeg: {}", line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Video transcoded successfully: {}", inputPath);
                return true;
            } else {
                logger.error("FFmpeg failed with exit code: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            logger.error("FFmpeg error: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取视频时长(秒)
     */
    public static int getVideoDuration(String inputPath) {
        ProcessBuilder pb = new ProcessBuilder(
                ffmpegPath,
                "-i", inputPath
        );
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Duration")) {
                        String duration = line.substring(line.indexOf("Duration:") + 10, line.indexOf(","));
                        String[] parts = duration.split(":");
                        if (parts.length == 3) {
                            int hours = Integer.parseInt(parts[0].trim());
                            int minutes = Integer.parseInt(parts[1].trim());
                            double seconds = Double.parseDouble(parts[2].trim());
                            return (int) (hours * 3600 + minutes * 60 + seconds);
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            logger.error("Failed to get video duration: {}", e.getMessage(), e);
        }
        return 0;
    }
}
