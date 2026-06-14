package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.component.RedisComponent;
import com.kilikili.config.Appconfig;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.VideoFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@RestController("webFileController")
@RequestMapping("/file")
@Validated
public class FileController extends ABaseController {

    @Autowired
    private VideoFileService videoFileService;

    @Autowired
    private Appconfig appconfig;

    @Autowired
    private RedisComponent redisComponent;



    @RequestMapping(method = RequestMethod.GET, value = "/getResource")
    public ResponseEntity<Resource> getResource(String sourceName) {
        File file = new File(sourceName);
        Resource resource = new FileSystemResource(file);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

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
        return getSuccessResponseVO(videoFileService.uploadImage(tempFile.getAbsolutePath(), createThumbnail));
    }

    @RequestMapping("/videoResource/{fileId}")
    public void videoResource(@PathVariable String fileId, HttpServletResponse response) throws Exception {
        String baseFolder = appconfig.getProjectFolder();
        File file = new File(baseFolder, "video/" + fileId + "/index.m3u8");
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType("application/vnd.apple.mpegurl");
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
    }

    @RequestMapping("/videoResourceTs/{fileId}/{ts}")
    public void videoResourceTs(@PathVariable String fileId, @PathVariable String ts, HttpServletResponse response) throws Exception {
        String baseFolder = appconfig.getProjectFolder();
        File file = new File(baseFolder, "video/" + fileId + "/" + ts);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType("video/mp2t");
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
    }
}
