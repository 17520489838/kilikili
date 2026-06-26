package com.kilikili.service.impl;

import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.Danmu;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.DanmuQuery;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.mappers.DanmuMapper;
import com.kilikili.mappers.UserInfoMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.DanmuService;
import com.kilikili.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

import static com.kilikili.entity.constants.Constants.LENGTH_10;

@Service("danmuService")
public class DanmuServiceImpl implements DanmuService {

    @Resource
    private DanmuMapper danmuMapper;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private RedisUtils<Object> redisUtils;

    private static final String REDIS_KEY_DANMU = Constants.REDIS_KEY_PREFIX + "danmu:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postDanmu(TokenUserInfoDto token, String videoId, String fileId,
                          String text, Integer mode, String color, Integer time) {
        Danmu danmu = new Danmu();
        danmu.setDanmuId(StringTools.getRandomNumber(LENGTH_10));
        danmu.setVideoId(videoId);
        danmu.setFileId(fileId);
        danmu.setUserId(token != null ? token.getUserId() : null);
        danmu.setContent(text);
        danmu.setTimePoint(time);
        danmu.setMode(mode != null ? mode : 1);
        danmu.setColor(color != null ? color : "#FFFFFF");
        danmu.setFontSize(25);
        danmu.setCreateTime(new Date());
        danmuMapper.insert(danmu);

        // Increment danmu count on the video
        videoMapper.updateCount(videoId, "danmu_count", 1);

        // Clear cache so it gets refreshed on next load
        String cacheKey = REDIS_KEY_DANMU + fileId + "_" + videoId;
        redisUtils.delete(cacheKey);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Danmu> loadDanmu(String fileId, String videoId) {
        // Try cache first
        String cacheKey = REDIS_KEY_DANMU + fileId + "_" + videoId;
        List<Danmu> cachedList = (List<Danmu>) redisUtils.get(cacheKey);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        // Query by fileId and/or videoId using DanmuQuery
        DanmuQuery query = new DanmuQuery();
        if (fileId != null && !fileId.isEmpty()) {
            query.setFileId(fileId);
        }
        if (videoId != null && !videoId.isEmpty()) {
            query.setVideoId(videoId);
        }
        List<Danmu> list = danmuMapper.selectListByCondition(query);

        // Cache in Redis
        if (list != null && !list.isEmpty()) {
            redisUtils.setex(cacheKey, (Object) list, 3600000L); // 1 hour cache
        }
        return list;
    }

    @Override
    public PaginationResultVO<Map<String, Object>> loadDanmuByPage(Integer pageNo, Integer pageSize, String videoId, String textFuzzy) {
        DanmuQuery query = new DanmuQuery();
        query.setPageNo(pageNo != null ? pageNo : 1);
        query.setPageSize(pageSize != null ? pageSize : 20);
        query.setVideoId(videoId);
        query.setTextFuzzy(textFuzzy);
        query.setOrderBy("create_time");
        query.setOrderDirection("desc");

        Long count = danmuMapper.selectCountByCondition(query);
        SimplePage page = new SimplePage(pageNo, query.getPageSize(), count);
        List<Danmu> list = danmuMapper.selectListByCondition(query);

        // Enrich with user and video info
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Danmu d : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("danmuId", d.getDanmuId());
            map.put("content", d.getContent());
            map.put("timePoint", d.getTimePoint());
            map.put("createTime", d.getCreateTime());
            map.put("videoId", d.getVideoId());
            map.put("userId", d.getUserId());

            // Get user nickname
            if (d.getUserId() != null) {
                UserInfo user = userInfoMapper.selectByUserId(d.getUserId());
                map.put("nickName", user != null ? user.getNickName() : d.getUserId());
            } else {
                map.put("nickName", "匿名");
            }

            // Get video name
            if (d.getVideoId() != null) {
                Video video = videoMapper.selectByVideoId(d.getVideoId());
                map.put("videoName", video != null ? video.getVideoName() : d.getVideoId());
            } else {
                map.put("videoName", "-");
            }

            resultList.add(map);
        }
        return new PaginationResultVO<>(page, resultList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delDanmu(String danmuId) {
        danmuMapper.deleteByDanmuId(danmuId);
    }
}
