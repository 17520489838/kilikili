package com.kilikili.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * FFmpeg工具类 - 用于视频转码
 */
public class FFmpegUtils {

    private static final Logger logger = LoggerFactory.getLogger(FFmpegUtils.class);

    private static String ffmpegPath = "ffmpeg";
    private static String h264Encoder = null; // auto-detect

    public static void setFfmpegPath(String path) {
        ffmpegPath = path;
        h264Encoder = null; // reset so next call re-detects
    }

    /**
     * 检测可用的H.264编码器
     */
    private static String detectH264Encoder() {
        if (h264Encoder != null) return h264Encoder;

        try {
            List<String> command = new ArrayList<>();
            command.add(ffmpegPath);
            command.add("-encoders");

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor();

            String encoders = output.toString();

            // Prefer libx264 (software, most compatible)
            if (encoders.contains("libx264")) {
                h264Encoder = "libx264";
                logger.info("FFmpeg H.264编码器: libx264");
                return h264Encoder;
            }
            // Fallback to h264_mf (MediaFoundation, available on all Windows 10+)
            if (encoders.contains("h264_mf")) {
                h264Encoder = "h264_mf";
                logger.info("FFmpeg H.264编码器: h264_mf (MediaFoundation)");
                return h264Encoder;
            }
            // Fallback to h264_amf (AMD)
            if (encoders.contains("h264_amf")) {
                h264Encoder = "h264_amf";
                logger.info("FFmpeg H.264编码器: h264_amf (AMD)");
                return h264Encoder;
            }
            // Fallback to h264_nvenc (NVIDIA)
            if (encoders.contains("h264_nvenc")) {
                h264Encoder = "h264_nvenc";
                logger.info("FFmpeg H.264编码器: h264_nvenc (NVIDIA)");
                return h264Encoder;
            }

            logger.warn("未找到H.264硬件编码器，将使用mpeg4(浏览器可能不兼容)");
            h264Encoder = "mpeg4";
            return h264Encoder;
        } catch (Exception e) {
            logger.error("检测编码器失败: {}", e.getMessage());
            h264Encoder = "libx264"; // best guess
            return h264Encoder;
        }
    }

    /**
     * 获取适合当前编码器的转码参数
     */
    private static List<String> buildVideoCodecArgs(String encoder) {
        List<String> args = new ArrayList<>();
        args.add("-c:v");
        args.add(encoder);

        switch (encoder) {
            case "libx264":
                args.add("-preset");
                args.add("medium");
                args.add("-crf");
                args.add("23");
                break;
            case "h264_mf":
                // MediaFoundation encoder: use bitrate instead of crf
                args.add("-b:v");
                args.add("2000k");
                args.add("-quality");
                args.add("90");
                break;
            case "h264_amf":
                args.add("-b:v");
                args.add("2000k");
                args.add("-quality");
                args.add("balanced");
                break;
            case "h264_nvenc":
                args.add("-b:v");
                args.add("2000k");
                args.add("-preset");
                args.add("medium");
                break;
            default:
                args.add("-b:v");
                args.add("2000k");
                break;
        }
        return args;
    }

    /**
     * 执行FFmpeg命令并收集输出
     */
    private static FfmpegResult execute(List<String> args) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.addAll(args);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        List<String> outputLines = new ArrayList<>();
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLines.add(line);
                    logger.debug("FFmpeg: {}", line);
                }
            }
            int exitCode = process.waitFor();

            // 收集最后20行输出用于错误诊断
            StringBuilder tail = new StringBuilder();
            int start = Math.max(0, outputLines.size() - 20);
            for (int i = start; i < outputLines.size(); i++) {
                tail.append(outputLines.get(i)).append("\n");
            }

            return new FfmpegResult(exitCode == 0, exitCode, tail.toString().trim());
        } catch (Exception e) {
            logger.error("FFmpeg execution error: {}", e.getMessage(), e);
            return new FfmpegResult(false, -1, e.getMessage());
        }
    }

    private static class FfmpegResult {
        final boolean success;
        final int exitCode;
        final String output;

        FfmpegResult(boolean success, int exitCode, String output) {
            this.success = success;
            this.exitCode = exitCode;
            this.output = output;
        }
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
        String encoder = detectH264Encoder();

        List<String> args = new ArrayList<>();
        args.add("-i");
        args.add(inputPath);
        args.addAll(buildVideoCodecArgs(encoder));
        args.add("-profile:v");
        args.add("baseline");
        args.add("-level");
        args.add("3.0");
        args.add("-start_number");
        args.add("0");
        args.add("-hls_time");
        args.add("10");
        args.add("-hls_list_size");
        args.add("0");
        args.add("-f");
        args.add("hls");
        args.add("-c:a");
        args.add("aac");
        args.add("-strict");
        args.add("-2");
        args.add(outputPath);

        FfmpegResult result = execute(args);
        if (result.success) {
            logger.info("HLS转码成功: {}", inputPath);
        } else {
            logger.error("HLS转码失败 (exit={}): {}\n{}", result.exitCode, inputPath, result.output);
        }
        return result.success;
    }

    /**
     * 将视频转码为MP4(H.264/AAC)格式
     */
    public static boolean convertToMp4(String inputPath, String outputPath) {
        String encoder = detectH264Encoder();
        logger.info("使用编码器 {} 转码MP4: {}", encoder, inputPath);

        List<String> args = new ArrayList<>();
        args.add("-i");
        args.add(inputPath);
        args.addAll(buildVideoCodecArgs(encoder));
        args.add("-c:a");
        args.add("aac");
        args.add("-b:a");
        args.add("128k");
        args.add("-movflags");
        args.add("+faststart");
        args.add("-y");
        args.add(outputPath);

        FfmpegResult result = execute(args);
        if (result.success) {
            logger.info("MP4转码成功: {}", outputPath);
        } else {
            logger.error("MP4转码失败 (exit={}): {}\n{}", result.exitCode, inputPath, result.output);
            // 如果 libx264 失败，尝试回退到 h264_mf
            if ("libx264".equals(encoder)) {
                logger.info("libx264失败，尝试回退到h264_mf...");
                h264Encoder = "h264_mf";
                return convertToMp4(inputPath, outputPath);
            }
        }
        return result.success;
    }

    /**
     * 提取视频指定时间点的一帧截图
     * @param inputPath  视频文件路径
     * @param outputPath 输出图片路径
     * @param time       时间点, 如 "00:00:01"
     * @return 是否成功
     */
    public static boolean extractFrame(String inputPath, String outputPath, String time) {
        List<String> args = new ArrayList<>();
        args.add("-i");
        args.add(inputPath);
        args.add("-ss");
        args.add(time);
        args.add("-vframes");
        args.add("1");
        args.add("-f");
        args.add("image2");
        args.add("-y");
        args.add(outputPath);

        FfmpegResult result = execute(args);
        if (result.success) {
            logger.info("视频帧提取成功: {}", outputPath);
        } else {
            logger.error("视频帧提取失败 (exit={}): {}\n{}", result.exitCode, inputPath, result.output);
        }
        return result.success;
    }

    /**
     * 获取视频时长(秒)
     */
    public static int getVideoDuration(String inputPath) {
        ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-i", inputPath);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("Duration")) {
                        try {
                            String duration = line.substring(line.indexOf("Duration:") + 10, line.indexOf(","));
                            String[] parts = duration.split(":");
                            if (parts.length == 3) {
                                int hours = Integer.parseInt(parts[0].trim());
                                int minutes = Integer.parseInt(parts[1].trim());
                                double seconds = Double.parseDouble(parts[2].trim());
                                return (int) (hours * 3600 + minutes * 60 + seconds);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            logger.error("获取视频时长失败: {}", e.getMessage(), e);
        }
        return 0;
    }
}
