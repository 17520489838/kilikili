package com.kilikili.service.impl;

import com.kilikili.entity.enums.VideoStatusEnum;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.CommentQuery;
import com.kilikili.entity.query.UserInfoQuery;
import com.kilikili.entity.query.VideoQuery;
import com.kilikili.mappers.CommentMapper;
import com.kilikili.mappers.UserInfoMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.service.StatisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("statisticsService")
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private VideoMapper videoMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private CommentMapper commentMapper;

    @Override
    public Map<String, Object> getActualTimeStatisticsInfo() {
        Map<String, Object> result = new HashMap<>();

        Long userCount = userInfoMapper.selectCountByCondition(new UserInfoQuery());
        result.put("userCount", userCount);

        VideoQuery videoQuery = new VideoQuery();
        videoQuery.setStatus(VideoStatusEnum.PUBLISHED.getCode());
        Long videoCount = videoMapper.selectCountByCondition(videoQuery);
        result.put("videoCount", videoCount);

        Long commentCount = commentMapper.selectCountByCondition(new CommentQuery());
        result.put("commentCount", commentCount);

        List<Video> allVideos = videoMapper.selectList();
        long totalPlayCount = allVideos.stream()
                .mapToLong(v -> v.getPlayCount() != null ? v.getPlayCount() : 0)
                .sum();
        result.put("totalPlayCount", totalPlayCount);

        return result;
    }

    @Override
    public Map<String, Object> getWeekStatisticsInfo() {
        Map<String, Object> result = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        Date startDate = calendar.getTime();

        VideoQuery videoQuery = new VideoQuery();
        videoQuery.setCreateTimeStart(startDate);
        videoQuery.setCreateTimeEnd(endDate);
        videoQuery.setPageNo(null);
        videoQuery.setPageSize(null);
        List<Video> videoList = videoMapper.selectListByCondition(videoQuery);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<String, Long> dayCountMap = new LinkedHashMap<>();

        calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        for (int i = 0; i < 7; i++) {
            dayCountMap.put(sdf.format(calendar.getTime()), 0L);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (Video video : videoList) {
            if (video.getCreateTime() != null) {
                String day = sdf.format(video.getCreateTime());
                dayCountMap.put(day, dayCountMap.getOrDefault(day, 0L) + 1);
            }
        }

        result.put("dayCount", dayCountMap);
        return result;
    }
}
