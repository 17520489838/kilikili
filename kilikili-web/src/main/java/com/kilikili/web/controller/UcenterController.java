package com.kilikili.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.enums.VideoStatusEnum;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.po.VideoFile;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.component.RedisComponent;
import com.kilikili.service.VideoFileService;
import com.kilikili.service.VideoService;
import com.kilikili.exception.BusinessException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@RestController("webUcenterController")
@RequestMapping("/ucenter")
@Validated
public class UcenterController extends ABaseController {

    @Resource
    private VideoService videoService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private VideoFileService videoFileService;

    // 发布视频
    @RequestMapping("/postVideo")
    public ResponseVO postVideo(String videoId, String videoCover, @NotEmpty String videoName,
                                @NotNull Integer pCategoryId, @NotNull Integer categoryId,
                                @NotNull Integer postType, String tags,
                                String introduction, String interaction, String uploadFileList) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        // 兼容前端只传单个 uploadId 或 fileId 的情况，包装成 JSON 数组
        String fileListJson = uploadFileList;
        boolean isUpdate = videoId != null && !videoId.isEmpty();
        if (!isUpdate) {
            try {
                JSONArray.parseArray(uploadFileList);
            } catch (JSONException e) {
                // 尝试通过 uploadId 查询真正的 fileId
                String resolvedFileId = uploadFileList;
                VideoFile vf = videoFileService.getVideoFileByFileId(uploadFileList);
                if (vf == null) {
                    vf = videoFileService.getVideoFileByUploadId(uploadFileList);
                }
                if (vf != null && vf.getFileId() != null) {
                    resolvedFileId = vf.getFileId();
                }
                JSONArray arr = new JSONArray();
                JSONObject obj = new JSONObject();
                obj.put("pName", "");
                obj.put("fileId", resolvedFileId);
                arr.add(obj);
                fileListJson = arr.toJSONString();
            }
        }
        String result = videoService.postVideo(tokenUserInfoDto, videoId, videoCover, videoName, pCategoryId, categoryId,
                postType, tags, introduction, interaction, isUpdate ? null : fileListJson);
        return getSuccessResponseVO(result);
    }

    // 加载视频列表
    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(Integer status, Integer pageNo, String videoNameFuzzy) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        return getSuccessResponseVO(videoService.loadVideoListByUserId(
                tokenUserInfoDto.getUserId(), status, videoNameFuzzy, pageNo));
    }

    // 获取视频数量信息
    @PostMapping("/getVideoCountInfo")
    public ResponseVO getVideoCountInfo() {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        String userId = tokenUserInfoDto.getUserId();
        Map<String, Integer> countInfo = new HashMap<>();
        for (VideoStatusEnum status : VideoStatusEnum.values()) {
            PaginationResultVO<Video> result = videoService.loadVideoListByUserId(userId, status.getCode(), null, 1);
            Long totalCount = result.getPageInfo().getTotalCount();
            countInfo.put(String.valueOf(status.getCode()), totalCount != null ? totalCount.intValue() : 0);
        }
        return getSuccessResponseVO(countInfo);
    }

    // 获取视频信息
    @PostMapping("/getVideoByVideoId")
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        return getSuccessResponseVO(videoService.getVideoByVideoIdForUser(videoId, tokenUserInfoDto.getUserId()));
    }

    // 保存视频互动信息
    @RequestMapping("/saveVideoInteraction")
    public ResponseVO saveVideoInteraction(@NotEmpty String videoId, String interaction) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        videoService.saveVideoInteraction(videoId, interaction, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    // 删除视频
    @RequestMapping("/deleteVideo")
    public ResponseVO deleteVideo(@NotEmpty String videoId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        videoService.deleteVideo(videoId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
