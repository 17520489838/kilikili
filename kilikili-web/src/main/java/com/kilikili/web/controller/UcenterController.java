package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.enums.VideoStatusEnum;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.VideoService;
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

    @RequestMapping("/postVideo")
    public ResponseVO postVideo(String videoCover, @NotEmpty String videoName, @NotNull Integer pCategoryId,
                                @NotNull Integer categoryId, @NotNull Integer postType, String tags,
                                String introduction, String interaction, @NotEmpty String uploadFileList) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        String videoId = videoService.postVideo(tokenUserInfoDto, videoCover, videoName, pCategoryId, categoryId,
                postType, tags, introduction, interaction, uploadFileList);
        return getSuccessResponseVO(videoId);
    }

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

    @PostMapping("/getVideoByVideoId")
    public ResponseVO getVideoByVideoId(@NotEmpty String videoId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        return getSuccessResponseVO(videoService.getVideoByVideoIdForUser(videoId, tokenUserInfoDto.getUserId()));
    }

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
