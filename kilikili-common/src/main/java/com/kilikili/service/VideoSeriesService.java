package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.VideoSeries;
import com.kilikili.entity.vo.PaginationResultVO;

import java.util.List;
import java.util.Map;

public interface VideoSeriesService {
    List<VideoSeries> loadVideoSeries(String userId);
    void saveVideoSeries(TokenUserInfoDto token, String seriesId, String seriesName, String seriesDescription);
    void delVideoSeries(String seriesId, String userId);
    void changeVideoSeriesSort(String seriesId, Integer sort, String userId);
    List<Map<String, Object>> loadAllVideo(String seriesId, String userId);
    void saveSeriesVideo(String seriesId, String videoId, String userId);
    void delSeriesVideo(String seriesId, String videoId, String userId);
    VideoSeries getVideoSeriesDetail(String seriesId);
    List<Map<String, Object>> loadVideoSeriesWithVideo(String userId);
}
