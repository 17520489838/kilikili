package com.kilikili.service.impl;

import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.po.UploadRecord;
import com.kilikili.entity.po.VideoFile;
import com.kilikili.entity.query.VideoFileQuery;
import com.kilikili.mappers.UploadRecordMapper;
import com.kilikili.mappers.VideoFileMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.VideoFileService;
import com.kilikili.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;

@Service("videoFileService")
public class VideoFileServiceImpl implements VideoFileService {

    @Resource
    private VideoFileMapper videoFileMapper;
    @Resource
    private UploadRecordMapper uploadRecordMapper;
    @Resource
    private RedisUtils<Object> redisUtils;

    // Temporary folder for chunk uploads
    private static final String TEMP_FOLDER = System.getProperty("java.io.tmpdir") + "/kilikili/upload/";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> preUploadVideo(String fileName, Integer chunks) {
        String uploadId = UUID.randomUUID().toString();

        // Create upload record
        UploadRecord record = new UploadRecord();
        record.setUploadId(uploadId);
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
    public String uploadVideo(String chunkFile, Integer chunkIndex, String uploadId) {
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
        return null;
    }

    /**
     * Merge all chunk files and create a VideoFile record
     * @return 生成的 fileId
     */
    private String mergeAndCreateVideoFile(UploadRecord record) {
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

        // Create VideoFile record
        String fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
        VideoFile videoFile = new VideoFile();
        videoFile.setFileId(fileId);
        videoFile.setFileName(record.getFileName());
        videoFile.setFilePath(mergedFile.getAbsolutePath());
        videoFile.setFileSize(mergedFile.length());
        videoFile.setUploadId(record.getUploadId());
        videoFile.setStatus(1); // upload complete
        videoFile.setCreateTime(new Date());
        videoFileMapper.insert(videoFile);

        // Cleanup temp chunk files
        File[] chunks = tempDir.listFiles();
        if (chunks != null) {
            for (File chunk : chunks) {
                chunk.delete();
            }
        }
        tempDir.delete();

        return fileId;
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
        // File path is handled by controller; return the path for DB storage
        return file;
    }

    @Override
    public VideoFile getVideoFileByFileId(String fileId) {
        return videoFileMapper.selectByFileId(fileId);
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
