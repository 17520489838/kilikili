package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.FileStatusEnum;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.po.VideoFile;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.VideoFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController("webFileController")
@RequestMapping("/file")
@Validated
public class FileController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private VideoFileService videoFileService;

    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/preUploadVideo")
    public ResponseVO preUploadVideo(@NotEmpty String fileName, Integer chunks) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        return getSuccessResponseVO(videoFileService.preUploadVideo(fileName, chunks, tokenUserInfoDto.getUserId()));
    }

    @PostMapping("/uploadVideo")
    public ResponseVO uploadVideo(@RequestParam("chunkFile") MultipartFile chunkFile,
                                  @RequestParam("chunkIndex") @NotEmpty String chunkIndex,
                                  @RequestParam("uploadId") @NotEmpty String uploadId) throws Exception {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        File tempFile = File.createTempFile("chunk_", chunkFile.getOriginalFilename());
        chunkFile.transferTo(tempFile);
        Map<String, Object> result = videoFileService.uploadVideo(tempFile.getAbsolutePath(), Integer.parseInt(chunkIndex), uploadId);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/delUploadVideo")
    public ResponseVO delUploadVideo(@NotEmpty String uploadId) {
        videoFileService.delUploadVideo(uploadId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/uploadImage")
    public ResponseVO uploadImage(MultipartFile file, Boolean createThumbnail) throws Exception {
        File tempFile = File.createTempFile("img_", file.getOriginalFilename());
        file.transferTo(tempFile);
        String ossUrl = videoFileService.uploadImage(tempFile.getAbsolutePath(), createThumbnail);
        return getSuccessResponseVO(ossUrl);
    }

    /**
     * 查询视频转码状态
     */
    @GetMapping("/transcodeStatus/{fileId}")
    public ResponseVO transcodeStatus(@PathVariable String fileId) {
        VideoFile videoFile = videoFileService.getVideoFileByFileId(fileId);
        Map<String, Object> result = new HashMap<>();
        if (videoFile == null) {
            result.put("status", -1);
            result.put("message", "文件不存在");
            return getSuccessResponseVO(result);
        }
        result.put("status", videoFile.getStatus());
        result.put("duration", videoFile.getDuration());
        result.put("fileName", videoFile.getFileName());

        FileStatusEnum statusEnum = FileStatusEnum.getByCode(videoFile.getStatus());
        result.put("message", statusEnum != null ? statusEnum.getDesc() : "未知");
        return getSuccessResponseVO(result);
    }

    /**
     * 获取视频资源文件（用于浏览器播放）
     * 返回已转码的 MP4 文件
     */
    @GetMapping("/videoResource/{fileId}")
    public ResponseEntity<Resource> videoResource(@PathVariable String fileId) {
        VideoFile videoFile = videoFileService.getVideoFileByFileId(fileId);
        if (videoFile == null) {
            logger.warn("videoResource: 文件不存在 fileId={}", fileId);
            return ResponseEntity.notFound().build();
        }

        Integer status = videoFile.getStatus();
        if (!FileStatusEnum.TRANSCODE_FINISH.getCode().equals(status)) {
            FileStatusEnum se = FileStatusEnum.getByCode(status);
            logger.warn("videoResource: 文件未就绪 fileId={}, status={}({}), filePath={}",
                    fileId, status, se != null ? se.getDesc() : "未知", videoFile.getFilePath());
            return ResponseEntity.notFound().build();
        }

        String filePath = videoFile.getFilePath();
        File video = new File(filePath);
        if (!video.exists()) {
            logger.error("videoResource: 文件不存在于磁盘 fileId={}, filePath={}", fileId, filePath);
            return ResponseEntity.notFound().build();
        }

        logger.info("videoResource: 返回视频 fileId={}, path={}, size={}", fileId, filePath, video.length());
        FileSystemResource resource = new FileSystemResource(video);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }

    /**
     * 获取原始视频文件（OSS 重定向）
     */
    @GetMapping("/videoOriginal/{fileId}")
    public ResponseEntity<Resource> videoOriginal(@PathVariable String fileId) {
        VideoFile videoFile = videoFileService.getVideoFileByFileId(fileId);
        if (videoFile == null) {
            return ResponseEntity.notFound().build();
        }

        String url = videoFile.getFilePath();
        if (url != null && url.startsWith("http")) {
            return ResponseEntity.status(302)
                    .header("Location", url)
                    .build();
        }

        File file = new File(url);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file));
    }
}
