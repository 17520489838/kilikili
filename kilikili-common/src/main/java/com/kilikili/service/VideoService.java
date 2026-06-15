package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.VideoQuery;
import com.kilikili.entity.vo.PaginationResultVO;

import java.util.List;

public interface VideoService {
    PaginationResultVO<Video> loadVideoPage(VideoQuery query);
    Video getVideoByVideoId(String videoId);
    List<Video> loadRecommendVideo();
    List<Video> loadHotVideoList();
    List<Video> getVideoRecommend();
    String postVideo(TokenUserInfoDto token, String videoId, String videoCover, String videoName, Integer pCategoryId, Integer categoryId, Integer postType, String tags, String introduction, String interaction, String uploadFileList);
    PaginationResultVO<Video> loadVideoListByUserId(String userId, Integer status, String videoNameFuzzy, Integer pageNo);
    Video getVideoByVideoIdForUser(String videoId, String userId);
    void saveVideoInteraction(String videoId, String interaction, String userId);
    void deleteVideo(String videoId, String userId);
}
