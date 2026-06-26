package com.kilikili.service.impl;

import com.kilikili.entity.enums.VideoStatusEnum;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.po.UserWatchHistory;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.*;
import com.kilikili.mappers.*;
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
    @Resource
    private UserWatchHistoryMapper userWatchHistoryMapper;
    @Resource
    private DanmuMapper danmuMapper;

    @Override
    public Map<String, Object> getActualTimeStatisticsInfo() {
        Map<String, Object> result = new HashMap<>();

        // Total user count
        Long userCount = userInfoMapper.selectCountByCondition(new UserInfoQuery());
        result.put("userCount", userCount);
        result.put("userCount", userCount);

        // Published video count
        VideoQuery videoQuery = new VideoQuery();
        videoQuery.setStatus(VideoStatusEnum.PUBLISHED.getCode());
        Long videoCount = videoMapper.selectCountByCondition(videoQuery);
        result.put("videoCount", videoCount);

        // Total comment count
        Long commentCount = commentMapper.selectCountByCondition(new CommentQuery());
        result.put("commentCount", commentCount);

        // Total danmu count
        Long danmuCount = danmuMapper.selectCountByCondition(new DanmuQuery());
        result.put("danmuCount", danmuCount);

        // Total play count (sum of all videos' play_count)
        List<Video> allVideos = videoMapper.selectList();
        long totalPlayCount = allVideos.stream()
                .mapToLong(v -> v.getPlayCount() != null ? v.getPlayCount() : 0)
                .sum();
        result.put("playCount", totalPlayCount);
        result.put("totalPlayCount", totalPlayCount);

        // Today's play count (watch_history watched_at >= start of today)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        Long todayPlayCount = userWatchHistoryMapper.selectCountByDateRange(todayStart, null);
        result.put("todayPlayCount", todayPlayCount != null ? todayPlayCount : 0);

        // Today's registration count
        UserInfoQuery regQuery = new UserInfoQuery();
        regQuery.setJoinTimeStart(todayStart);
        Long todayRegistCount = userInfoMapper.selectCountByCondition(regQuery);
        result.put("todayRegistCount", todayRegistCount != null ? todayRegistCount : 0);

        return result;
    }

    @Override
    public Map<String, Object> getWeekStatisticsInfo() {
        Map<String, Object> result = new HashMap<>();

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -6);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Build date list and initialize day maps
        List<String> dateList = new ArrayList<>();
        Map<String, Long> userDayMap = new LinkedHashMap<>();
        Map<String, Long> videoDayMap = new LinkedHashMap<>();
        Map<String, Long> playDayMap = new LinkedHashMap<>();

        Calendar iterCal = Calendar.getInstance();
        iterCal.setTime(startDate);
        for (int i = 0; i < 7; i++) {
            String dayStr = sdf.format(iterCal.getTime());
            dateList.add(dayStr);
            userDayMap.put(dayStr, 0L);
            videoDayMap.put(dayStr, 0L);
            playDayMap.put(dayStr, 0L);
            iterCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Query users created this week
        UserInfoQuery userQuery = new UserInfoQuery();
        userQuery.setJoinTimeStart(startDate);
        userQuery.setJoinTimeEnd(endDate);
        userQuery.setPageNo(null);
        userQuery.setPageSize(null);
        List<UserInfo> userList = userInfoMapper.selectListByCondition(userQuery);
        for (UserInfo u : userList) {
            if (u.getJoinTime() != null) {
                String day = sdf.format(u.getJoinTime());
                userDayMap.put(day, userDayMap.getOrDefault(day, 0L) + 1);
            }
        }

        // Query videos created this week
        VideoQuery videoQuery = new VideoQuery();
        videoQuery.setCreateTimeStart(startDate);
        videoQuery.setCreateTimeEnd(endDate);
        videoQuery.setPageNo(null);
        videoQuery.setPageSize(null);
        List<Video> videoList = videoMapper.selectListByCondition(videoQuery);
        for (Video v : videoList) {
            if (v.getCreateTime() != null) {
                String day = sdf.format(v.getCreateTime());
                videoDayMap.put(day, videoDayMap.getOrDefault(day, 0L) + 1);
            }
        }

        // Query watch history this week
        List<UserWatchHistory> whList = userWatchHistoryMapper.selectListByDateRange(startDate, endDate);
        for (UserWatchHistory wh : whList) {
            if (wh.getWatchedAt() != null) {
                String day = sdf.format(wh.getWatchedAt());
                playDayMap.put(day, playDayMap.getOrDefault(day, 0L) + 1);
            }
        }

        // Build result arrays
        List<Long> userCountList = new ArrayList<>();
        List<Long> videoCountList = new ArrayList<>();
        List<Long> playCountList = new ArrayList<>();
        for (String date : dateList) {
            userCountList.add(userDayMap.getOrDefault(date, 0L));
            videoCountList.add(videoDayMap.getOrDefault(date, 0L));
            playCountList.add(playDayMap.getOrDefault(date, 0L));
        }

        result.put("dateList", dateList);
        result.put("userCountList", userCountList);
        result.put("videoCountList", videoCountList);
        result.put("playCountList", playCountList);
        // Include danmuCountList for compatibility (danmu doesn't have create_time tracking, use 0s)
        List<Long> danmuCountList = new ArrayList<>();
        for (int i = 0; i < 7; i++) danmuCountList.add(0L);
        result.put("danmuCountList", danmuCountList);

        return result;
    }
}
