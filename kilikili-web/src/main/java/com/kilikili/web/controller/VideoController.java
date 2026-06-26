package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.po.VideoP;
import com.kilikili.entity.query.VideoPQuery;
import com.kilikili.entity.query.VideoQuery;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.mappers.VideoPMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.VideoService;
import com.kilikili.service.UserWatchHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("webVideoController")
@RequestMapping("/video")
@Validated
public class VideoController extends ABaseController {

    private static final String REDIS_KEY_VIDEO_ONLINE = "kilikili:video:online:";
    private static final String REDIS_KEY_SEARCH_HOT = "kilikili:search:hot";

    @Resource
    private VideoService videoService;

    @Resource
    private VideoPMapper videoPMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserWatchHistoryService userWatchHistoryService;

    @RequestMapping("/loadRecommendVideo")
    public ResponseVO loadRecommendVideo() {
        List<Video> result = videoService.loadRecommendVideo();
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/loadVideo")
    public ResponseVO loadVideo(Integer pCategoryId, Integer categoryId,
                                @NotNull @Min(1) Integer pageNo) {
        VideoQuery query = new VideoQuery();
        query.setPCategoryId(pCategoryId);
        query.setCategoryId(categoryId);
        query.setStatus(3); // only show published videos
        query.setPageNo(pageNo);
        PaginationResultVO<Video> result = videoService.loadVideoPage(query);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/getVideoInfo")
    public ResponseVO getVideoInfo(@NotEmpty String videoId) {
        Video video = videoService.getVideoByVideoId(videoId);
        if (video == null) {
            throw new BusinessException(ResponseCodeEnum.NOT_FOUND);
        }
        // Increment play count in database
        videoMapper.updateCount(videoId, "play_count", 1);

        // Optionally check for token and get user-specific data (like/focus status)
        String token = getTokenFromCookie();
        if (token != null && !token.isEmpty()) {
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
            if (tokenUserInfoDto != null) {
                // Record watch history for logged-in user
                userWatchHistoryService.recordWatch(tokenUserInfoDto.getUserId(), videoId);
            }
        }

        return getSuccessResponseVO(video);
    }

    @RequestMapping("/loadVideoPList")
    public ResponseVO loadVideoPList(@NotEmpty String videoId) {
        VideoPQuery query = new VideoPQuery();
        query.setVideoId(videoId);
        List<VideoP> list = videoPMapper.selectListByCondition(query);
        return getSuccessResponseVO(list);
    }

    @RequestMapping("/reportVideoPlayOnline")
    public ResponseVO reportVideoPlayOnline(@NotEmpty String fileId, @NotEmpty String deviceId) {
        String key = REDIS_KEY_VIDEO_ONLINE + fileId + ":" + deviceId;
        redisUtils.incrementex(key, 300000L);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/search")
    public ResponseVO search(@NotEmpty String keyword, @NotNull @Min(1) Integer pageNo) {
        // Save search keyword to Redis ZSet for hot search ranking
        redisUtils.zaddCount(REDIS_KEY_SEARCH_HOT, keyword);

        VideoQuery query = new VideoQuery();
        query.setKeyword(keyword);
        query.setStatus(3); // only search published videos
        query.setPageNo(pageNo);
        PaginationResultVO<Video> result = videoService.loadVideoPage(query);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/getSearchKeywordTop")
    public ResponseVO getSearchKeywordTop() {
        List hotKeywords = redisUtils.getZSetList(REDIS_KEY_SEARCH_HOT, 10);
        return getSuccessResponseVO(hotKeywords);
    }

    @RequestMapping("/getVideoRecommend")
    public ResponseVO getVideoRecommend() {
        List<Video> result = videoService.getVideoRecommend();
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/loadHotVideoList")
    public ResponseVO loadHotVideoList() {
        List<Video> result = videoService.loadHotVideoList();
        return getSuccessResponseVO(result);
    }
}
