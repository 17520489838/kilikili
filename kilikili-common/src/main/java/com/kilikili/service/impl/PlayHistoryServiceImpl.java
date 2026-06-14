package com.kilikili.service.impl;

import com.kilikili.entity.po.PlayHistory;
import com.kilikili.entity.query.PlayHistoryQuery;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.mappers.PlayHistoryMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.service.PlayHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("playHistoryService")
public class PlayHistoryServiceImpl implements PlayHistoryService {

    @Resource
    private PlayHistoryMapper playHistoryMapper;
    @Resource
    private VideoMapper videoMapper;

    @Override
    public PaginationResultVO<PlayHistory> loadHistory(String userId, Integer pageNo) {
        PlayHistoryQuery query = new PlayHistoryQuery();
        query.setUserId(userId);
        query.setPageNo(pageNo);
        query.setOrderBy("last_play_time");
        query.setOrderDirection("desc");

        Long count = playHistoryMapper.selectCountByCondition(query);
        SimplePage page = new SimplePage(pageNo, query.getPageSize(), count);
        List<PlayHistory> list = playHistoryMapper.selectListByCondition(query);
        return new PaginationResultVO<>(page, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delHistory(String userId, String videoId) {
        playHistoryMapper.deleteByUserIdVideoId(userId, videoId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanHistory(String userId) {
        playHistoryMapper.deleteByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveHistory(String userId, String videoId, String fileId, Integer progress, Integer duration) {
        PlayHistory history = playHistoryMapper.selectByUserIdVideoId(userId, videoId);
        if (history != null) {
            history.setFileId(fileId);
            history.setProgress(progress);
            history.setDuration(duration);
            history.setLastPlayTime(new Date());
            history.setUpdateTime(new Date());
            playHistoryMapper.updateByUserIdVideoId(history);
        } else {
            history = new PlayHistory();
            history.setUserId(userId);
            history.setVideoId(videoId);
            history.setFileId(fileId);
            history.setProgress(progress);
            history.setDuration(duration);
            history.setLastPlayTime(new Date());
            history.setCreateTime(new Date());
            history.setUpdateTime(new Date());
            playHistoryMapper.insert(history);
        }
    }
}
