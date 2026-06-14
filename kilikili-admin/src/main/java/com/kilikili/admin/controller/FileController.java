package com.kilikili.admin.controller;

import com.kilikili.config.Appconfig;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.service.VideoFileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Autowired;
import java.io.File;

@RestController("adminFileController")
@RequestMapping("/file")
@Validated
public class FileController extends ABaseController {

    @Autowired
    private VideoFileService videoFileService;

    @Autowired
    private Appconfig appconfig;

    @RequestMapping("/uploadImage")
    public ResponseVO uploadImage(MultipartFile file, Boolean createThumbnail) throws Exception {
        if (file == null || file.isEmpty()) {
            return getSuccessResponseVO(null);
        }
        String projectFolder = appconfig.getProjectFolder();
        String fileName = file.getOriginalFilename();
        String targetDir = projectFolder + "temp/";
        File dir = new File(targetDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File tempFile = new File(dir, fileName);
        file.transferTo(tempFile);
        return getSuccessResponseVO(videoFileService.uploadImage(tempFile.getAbsolutePath(), createThumbnail));
    }

    @RequestMapping("/getResource")
    public ResponseEntity<Resource> getResource(String sourceName) {
        File file = new File(appconfig.getProjectFolder(), sourceName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    @RequestMapping("/videoResource")
    public ResponseEntity<Resource> videoResource(String fileId) {
        File file = new File(appconfig.getProjectFolder(), "video/" + fileId + "/index.m3u8");
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                .body(resource);
    }

    @RequestMapping("/videoResourceTs")
    public ResponseEntity<Resource> videoResourceTs(String fileId, String ts) {
        File file = new File(appconfig.getProjectFolder(), "video/" + fileId + "/" + ts);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("video/MP2T"))
                .body(resource);
    }
}
