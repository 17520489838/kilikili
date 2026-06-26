package com.kilikili.service.impl;

import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.po.UserWatchHistory;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.mappers.UserInfoMapper;
import com.kilikili.mappers.UserWatchHistoryMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.service.UserWatchHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service("userWatchHistoryService")
public class UserWatchHistoryServiceImpl implements UserWatchHistoryService {

    @Resource
    private UserWatchHistoryMapper userWatchHistoryMapper;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordWatch(String userId, String videoId) {
        userWatchHistoryMapper.insertOrUpdate(userId, videoId);
    }

    @Override
    public PaginationResultVO<Map<String, Object>> loadHistory(String userId, Integer pageNo) {
        if (pageNo == null || pageNo < 1) pageNo = 1;
        int pageSize = 20;

        Long totalCount = userWatchHistoryMapper.countByUserId(userId);
        SimplePage page = new SimplePage(pageNo, pageSize, totalCount);

        List<UserWatchHistory> records = userWatchHistoryMapper.selectRecentByUserIdOffset(
                userId, page.getStartIndex(), pageSize);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (UserWatchHistory wh : records) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("videoId", wh.getVideoId());
            map.put("lastPlayTime", wh.getWatchedAt());

            Video video = videoMapper.selectByVideoId(wh.getVideoId());
            if (video != null) {
                map.put("videoName", video.getVideoName());
                map.put("videoCover", video.getVideoCover());
                map.put("duration", video.getDuration());
                UserInfo user = userInfoMapper.selectByUserId(video.getUserId());
                map.put("nickName", user != null ? user.getNickName() : "");
            }
            resultList.add(map);
        }

        return new PaginationResultVO<>(page, resultList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delHistory(String userId, String videoId) {
        userWatchHistoryMapper.deleteByUserIdVideoId(userId, videoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanHistory(String userId) {
        userWatchHistoryMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanUpOldRecords(String userId, int keepCount) {
        Long total = userWatchHistoryMapper.countByUserId(userId);
        if (total > keepCount) {
            List<UserWatchHistory> oldRecords = userWatchHistoryMapper.selectOldByUserId(userId, keepCount);
            for (UserWatchHistory record : oldRecords) {
                userWatchHistoryMapper.deleteByUserIdVideoId(record.getUserId(), record.getVideoId());
            }
        }
    }
}
