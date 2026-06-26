package com.kilikili.service.impl;

import com.aliyun.oss.OSS;
import com.kilikili.config.Appconfig;
import com.kilikili.config.OssConfig;
import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.po.UploadRecord;
import com.kilikili.entity.po.VideoFile;
import com.kilikili.entity.query.VideoFileQuery;
import com.kilikili.mappers.UploadRecordMapper;
import com.kilikili.mappers.VideoFileMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.VideoFileService;
import com.kilikili.service.VideoTranscodeService;
import com.kilikili.utils.FFmpegUtils;
import com.kilikili.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service("videoFileService")
public class VideoFileServiceImpl implements VideoFileService {

    private static final Logger logger = LoggerFactory.getLogger(VideoFileServiceImpl.class);

    @Resource
    private VideoFileMapper videoFileMapper;
    @Resource
    private UploadRecordMapper uploadRecordMapper;
    @Resource
    private RedisUtils<Object> redisUtils;
    @Resource
    private OSS ossClient;
    @Resource
    private OssConfig ossConfig;

    @Resource
    private VideoTranscodeService videoTranscodeService;

    @Resource
    private Appconfig appconfig;

    // Temporary folder for chunk uploads
    private static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "/kilikili/upload/";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> preUploadVideo(String fileName, Integer chunks, String userId) {
        String uploadId = UUID.randomUUID().toString();

        // Create upload record
        UploadRecord record = new UploadRecord();
        record.setUploadId(uploadId);
        record.setUserId(userId);
        record.setFileName(fileName);
        record.setChunkCount(chunks);
        record.setUploadedChunks(0);
        record.setStatus(0);
        uploadRecordMapper.insert(record);

        // Check for already uploaded chunks (for resume support)
        List<Integer> existChunks = new ArrayList<>();
        File tempDir = new File(TEMP_FOLDER + uploadId);
        if (tempDir.exists()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    try {
                        existChunks.add(Integer.parseInt(f.getName()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("uploadId", uploadId);
        result.put("existChunks", existChunks);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> uploadVideo(String chunkFile, Integer chunkIndex, String uploadId) {
        UploadRecord record = uploadRecordMapper.selectByUploadId(uploadId);
        if (record == null) {
            throw new RuntimeException("上传记录不存在");
        }

        // Save chunk file to temp directory
        File tempDir = new File(TEMP_FOLDER + uploadId);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // The chunkFile parameter is a file path; copy to temp directory
        File srcFile = new File(chunkFile);
        File destFile = new File(tempDir, String.valueOf(chunkIndex));
        if (srcFile.exists()) {
            try (FileInputStream fis = new FileInputStream(srcFile);
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            } catch (IOException e) {
                throw new RuntimeException("保存分片失败", e);
            }
        }

        // Update uploaded chunks count
        record.setUploadedChunks(record.getUploadedChunks() + 1);
        uploadRecordMapper.updateByUploadId(record);

        // When all chunks are done, merge and create VideoFile record
        if (record.getUploadedChunks() >= record.getChunkCount()) {
            record.setStatus(1);
            uploadRecordMapper.updateByUploadId(record);
            return mergeAndCreateVideoFile(record);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("fileId", null);
        result.put("videoId", null);
        return result;
    }

    /**
     * Merge all chunk files and create VideoFile record
     * @return Map containing fileId
     */
    private Map<String, Object> mergeAndCreateVideoFile(UploadRecord record) {
        File tempDir = new File(TEMP_FOLDER + record.getUploadId());
        File mergedFile = new File(TEMP_FOLDER + record.getUploadId() + "_merged");

        // Merge chunks
        try (FileOutputStream fos = new FileOutputStream(mergedFile)) {
            for (int i = 0; i < record.getChunkCount(); i++) {
                File chunk = new File(tempDir, String.valueOf(i));
                if (!chunk.exists()) {
                    throw new RuntimeException("分片缺失: " + i);
                }
                try (FileInputStream fis = new FileInputStream(chunk)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("合并文件失败", e);
        }

        String fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
        String filePath;

        // Upload merged file to OSS (非致命，失败则使用本地路径)
        String objectKey = "video/" + fileId + "/" + record.getFileName();
        try (FileInputStream fis = new FileInputStream(mergedFile)) {
            ossClient.putObject(ossConfig.getBucketName(), objectKey, fis);
            filePath = "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectKey;
            logger.info("视频文件上传至OSS成功: {}/{}", ossConfig.getBucketName(), objectKey);
        } catch (Exception e) {
            logger.warn("OSS上传失败(将使用本地路径): {}", e.getMessage());
            filePath = "local:" + mergedFile.getAbsolutePath();
        }

        // Create VideoFile record
        VideoFile videoFile = new VideoFile();
        videoFile.setFileId(fileId);
        videoFile.setFileName(record.getFileName());
        videoFile.setFilePath(filePath);
        videoFile.setFileSize(mergedFile.length());
        videoFile.setUploadId(record.getUploadId());
        videoFile.setStatus(1); // upload complete
        videoFile.setCreateTime(new Date());
        videoFileMapper.insert(videoFile);

        // Move merged file to permanent location (supports cross-drive via NIO)
        String originalVideoDir = getOriginalVideoDir(fileId);
        new File(originalVideoDir).mkdirs();
        File permanentFile = new File(originalVideoDir, record.getFileName());
        try {
            Files.move(mergedFile.toPath(), permanentFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("移动合并文件到永久目录失败", e);
        }

        // Cleanup temp chunk directory only
        cleanupChunkDir(tempDir);

        // Extract video cover from first frame
        String tempCoverDir = System.getProperty("java.io.tmpdir") + "/kilikili/cover/";
        new File(tempCoverDir).mkdirs();
        String coverPath = tempCoverDir + fileId + ".png";
        boolean frameOk = FFmpegUtils.extractFrame(permanentFile.getAbsolutePath(), coverPath, "00:00:01");
        if (!frameOk) {
            frameOk = FFmpegUtils.extractFrame(permanentFile.getAbsolutePath(), coverPath, "00:00:00");
        }
        if (frameOk) {
            try {
                String coverUrl = uploadImage(coverPath, false);
                videoFile.setVideoCover(coverUrl);
                videoFileMapper.updateByFileId(videoFile);
                logger.info("视频封面自动生成成功: {}", coverUrl);
            } catch (Exception e) {
                logger.warn("封面上传失败(不影响主流程): {}", e.getMessage());
                new File(coverPath).delete();
            }
        } else {
            logger.warn("视频帧提取失败(不影响主流程), fileId={}", fileId);
        }

        // Trigger async transcoding
        videoTranscodeService.startTranscode(fileId, permanentFile.getAbsolutePath());

        Map<String, Object> result = new HashMap<>();
        result.put("fileId", fileId);
        return result;
    }

    private String getOriginalVideoDir(String fileId) {
        return appconfig.getProjectFolder() + "video/original/" + fileId + "/";
    }

    private void cleanupChunkDir(File tempDir) {
        if (tempDir.exists()) {
            File[] chunks = tempDir.listFiles();
            if (chunks != null) {
                for (File chunk : chunks) {
                    chunk.delete();
                }
            }
            tempDir.delete();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delUploadVideo(String uploadId) {
        UploadRecord record = uploadRecordMapper.selectByUploadId(uploadId);
        if (record != null) {
            // Delete temp directory and files
            File tempDir = new File(TEMP_FOLDER + uploadId);
            if (tempDir.exists()) {
                File[] files = tempDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
                tempDir.delete();
            }

            // Delete merged file
            File mergedFile = new File(TEMP_FOLDER + uploadId + "_merged");
            if (mergedFile.exists()) {
                mergedFile.delete();
            }

            // Delete upload record
            uploadRecordMapper.updateByUploadId(record);
        }
    }

    @Override
    public String uploadImage(String file, Boolean createThumbnail) {
        File imageFile = new File(file);
        if (!imageFile.exists()) {
            throw new RuntimeException("图片文件不存在: " + file);
        }

        String fileName = imageFile.getName();
        String ext = "";
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            ext = fileName.substring(dotIndex);
        }

        String objectKey = "image/" + StringTools.getRandomNumber(Constants.LENGTH_10) + ext;
        try (FileInputStream fis = new FileInputStream(imageFile)) {
            ossClient.putObject(ossConfig.getBucketName(), objectKey, fis);
            logger.info("图片上传至OSS成功: {}/{}", ossConfig.getBucketName(), objectKey);
        } catch (IOException e) {
            throw new RuntimeException("上传图片到OSS失败", e);
        }

        // Clean up temp file
        imageFile.delete();

        return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectKey;
    }

    @Override
    public VideoFile getVideoFileByFileId(String fileId) {
        return videoFileMapper.selectByFileId(fileId);
    }

    @Override
    public VideoFile getVideoFileByUploadId(String uploadId) {
        return videoFileMapper.selectByUploadId(uploadId);
    }

    @Override
    public List<VideoFile> getVideoFileList(VideoFileQuery query) {
        return videoFileMapper.selectListByCondition(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveVideoFile(VideoFile videoFile) {
        if (videoFile.getFileId() == null) {
            videoFile.setFileId(StringTools.getRandomNumber(Constants.LENGTH_10));
        }
        videoFileMapper.insert(videoFile);
    }
}
