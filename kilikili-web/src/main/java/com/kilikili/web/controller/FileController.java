package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.VideoFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.io.File;

@RestController("webFileController")
@RequestMapping("/file")
@Validated
public class FileController extends ABaseController {

    @Autowired
    private VideoFileService videoFileService;

    @Autowired
    private RedisComponent redisComponent;

    @RequestMapping("/preUploadVideo")
    public ResponseVO preUploadVideo(@NotEmpty String fileName, Integer chunks) {
        return getSuccessResponseVO(videoFileService.preUploadVideo(fileName, chunks));
    }

    @PostMapping("/uploadVideo")
    public ResponseVO uploadVideo(@RequestParam("chunkFile") MultipartFile chunkFile,
                                  @RequestParam("chunkIndex") @NotEmpty String chunkIndex) throws Exception {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        String userId = tokenUserInfoDto.getUserId();
        
        File tempFile = File.createTempFile("chunk_", chunkFile.getOriginalFilename());
        chunkFile.transferTo(tempFile);
        String fileId = videoFileService.uploadVideo(tempFile.getAbsolutePath(), Integer.parseInt(chunkIndex), userId);
        return getSuccessResponseVO(fileId);
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
}
