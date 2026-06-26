package com.kilikili.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kilikili.component.RedisComponent;
import com.kilikili.config.Appconfig;
import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.VideoStatusEnum;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.po.VideoFile;
import com.kilikili.entity.po.VideoP;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.query.VideoPQuery;
import com.kilikili.entity.query.VideoQuery;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.mappers.UserInfoMapper;
import com.kilikili.mappers.VideoFileMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.mappers.VideoPMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.VideoFileService;
import com.kilikili.service.VideoService;
import com.kilikili.utils.FFmpegUtils;
import com.kilikili.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service("videoService")
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoMapper videoMapper;
    @Resource
    private VideoPMapper videoPMapper;
    @Resource
    private VideoFileMapper videoFileMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private RedisUtils<Object> redisUtils;
    @Resource
    private Appconfig appconfig;
    @Resource
    private VideoFileService videoFileService;

    private static final Logger logger = LoggerFactory.getLogger(VideoServiceImpl.class);
    private static final String REDIS_KEY_HOT_VIDEO = Constants.REDIS_KEY_PREFIX + "hot:video";
    private static final Integer HOT_VIDEO_LIMIT = 20;
    private static final Integer RECOMMEND_LIMIT = 20;
    private static final Integer VIDEO_RECOMMEND_LIMIT = 10;

    @Override
    public PaginationResultVO<Video> loadVideoPage(VideoQuery query) {
        Integer pageNo = query.getPageNo() != null ? query.getPageNo() : 1;
        Integer pageSize = query.getPageSize() != null ? query.getPageSize() : 10;

        Long totalCount = videoMapper.selectCountByCondition(query);
        SimplePage simplePage = new SimplePage(pageNo, pageSize, totalCount);

        query.setPageNo(pageNo);
        query.setPageSize(pageSize);

        List<Video> list = videoMapper.selectListByCondition(query);
        // 填充视频作者信息(用于后台管理展示)
        if (list != null && !list.isEmpty()) {
            java.util.Map<String, UserInfo> userCache = new java.util.HashMap<>();
            for (Video v : list) {
                if (v.getUserId() != null) {
                    UserInfo u = userCache.get(v.getUserId());
                    if (u == null) {
                        u = userInfoMapper.selectByUserId(v.getUserId());
                        if (u != null) userCache.put(v.getUserId(), u);
                    }
                    if (u != null) {
                        v.setUserName(u.getNickName());
                        v.setAvatar(u.getAvatar());
                    }
                }
            }
        }
        return new PaginationResultVO<>(simplePage, list);
    }

    @Override
    public Video getVideoByVideoId(String videoId) {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video != null) {
            // Also get user info for the video's uploader
            UserInfo userInfo = userInfoMapper.selectByUserId(video.getUserId());
            if (userInfo != null) {
                video.setUserName(userInfo.getNickName());
            }
            // Get fileId from VideoP for this video
            VideoPQuery vpQuery = new VideoPQuery();
            vpQuery.setVideoId(videoId);
            List<VideoP> vpList = videoPMapper.selectListByCondition(vpQuery);
            if (vpList != null && !vpList.isEmpty()) {
                video.setFileId(vpList.get(0).getFileId());
            }
        }
        return video;
    }

    @Override
    public List<Video> loadRecommendVideo() {
        VideoQuery query = new VideoQuery();
        query.setRecommendType(1);
        query.setStatus(VideoStatusEnum.PUBLISHED.getCode());
        List<Video> list = videoMapper.selectListByCondition(query);
        if (list == null) {
            return new ArrayList<>();
        }
        return list.size() > RECOMMEND_LIMIT ? list.subList(0, RECOMMEND_LIMIT) : list;
    }

    @Override
    public List<Video> loadHotVideoList() {
        // Try cache first from Redis sorted set
        List<Object> cachedIds = redisUtils.getZSetList(REDIS_KEY_HOT_VIDEO, HOT_VIDEO_LIMIT);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            List<Video> cachedVideos = new ArrayList<>();
            for (Object id : cachedIds) {
                Video v = videoMapper.selectByVideoId(String.valueOf(id));
                if (v != null) {
                    cachedVideos.add(v);
                }
            }
            if (!cachedVideos.isEmpty()) {
                return cachedVideos;
            }
        }

        // Query from DB: order by play_count DESC, status=PUBLISHED, limit 20
        VideoQuery query = new VideoQuery();
        query.setStatus(VideoStatusEnum.PUBLISHED.getCode());
        query.setOrderBy("play_count");
        query.setOrderDirection("desc");
        query.setPageNo(1);
        query.setPageSize(HOT_VIDEO_LIMIT);

        List<Video> list = videoMapper.selectListByCondition(query);
        if (list != null) {
            for (Video video : list) {
                redisUtils.zaddCount(REDIS_KEY_HOT_VIDEO, video.getVideoId());
            }
        }
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public List<Video> getVideoRecommend() {
        VideoQuery query = new VideoQuery();
        query.setStatus(VideoStatusEnum.PUBLISHED.getCode());
        List<Video> list = videoMapper.selectListByCondition(query);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        // Randomize and get 10
        Collections.shuffle(list);
        return list.size() > VIDEO_RECOMMEND_LIMIT ? list.subList(0, VIDEO_RECOMMEND_LIMIT) : list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String postVideo(TokenUserInfoDto token, String videoId, String videoCover, String videoName,
                          Integer pCategoryId, Integer categoryId, Integer postType,
                          String tags, String introduction, String interaction,
                          String uploadFileList) {
        try {
            return doPostVideo(token, videoId, videoCover, videoName, pCategoryId, categoryId, postType,
                    tags, introduction, interaction, uploadFileList);
        } catch (Exception e) {
            logger.error("postVideo 异常: videoName={}, uploadFileList={}, userId={}",
                    videoName, uploadFileList, token != null ? token.getUserId() : null, e);
            throw e;
        }
    }

    private String doPostVideo(TokenUserInfoDto token, String videoId, String videoCover, String videoName,
                             Integer pCategoryId, Integer categoryId, Integer postType,
                             String tags, String introduction, String interaction,
                             String uploadFileList) {
        // 更新模式：传入 videoId 时，跳过文件处理和 VideoP 重建，只更新元数据
        if (videoId != null && !videoId.isEmpty()) {
            Video existVideo = videoMapper.selectByVideoIdIncludeDeleted(videoId);
            if (existVideo == null) {
                throw new BusinessException("视频不存在");
            }
            if (!Objects.equals(existVideo.getUserId(), token.getUserId())) {
                throw new BusinessException("无权操作该视频");
            }
            if (videoCover != null && !videoCover.isEmpty()) {
                existVideo.setVideoCover(videoCover);
            } else if (existVideo.getVideoCover() == null || existVideo.getVideoCover().isEmpty()) {
                String firstFileId = getFirstFileId(videoId);
                if (firstFileId != null) {
                    // 优先使用 merge 时预生成的封面
                    VideoFile vf = videoFileMapper.selectByFileId(firstFileId);
                    if (vf != null && vf.getVideoCover() != null && !vf.getVideoCover().isEmpty()) {
                        existVideo.setVideoCover(vf.getVideoCover());
                    } else {
                        String coverUrl = autoGenerateCover(firstFileId);
                        if (coverUrl != null) {
                            existVideo.setVideoCover(coverUrl);
                        }
                    }
                }
            }
            existVideo.setVideoName(videoName);
            existVideo.setPCategoryId(pCategoryId);
            existVideo.setCategoryId(categoryId);
            existVideo.setPostType(postType);
            existVideo.setTags(tags);
            existVideo.setIntroduction(introduction);
            existVideo.setInteraction(interaction);
            videoMapper.updateByVideoId(existVideo);
            return videoId;
        }

        // 创建模式：解析 uploadFileList（JSON 数组或 uploadId）
        JSONArray fileArray = null;
        try {
            fileArray = JSONArray.parseArray(uploadFileList);
        } catch (Exception ignored) {}

        // 如果不是 JSON 数组，尝试当作 uploadId 查询
        if (fileArray == null || fileArray.isEmpty()) {
            VideoFile vf = videoFileMapper.selectByUploadId(uploadFileList);
            if (vf != null) {
                fileArray = new JSONArray();
                JSONObject obj = new JSONObject();
                obj.put("pName", vf.getFileName() != null ? vf.getFileName() : "default");
                obj.put("fileId", vf.getFileId());
                fileArray.add(obj);
            }
        }

        if (fileArray == null || fileArray.isEmpty()) {
            throw new BusinessException("上传文件列表不能为空");
        }

        // 生成新的 videoId（覆盖方法参数，创建模式时参数值为 null）
        videoId = StringTools.getRandomNumber(Constants.LENGTH_10);
        int totalDuration = 0;

        // Create VideoP records for each file, update VideoFile userId
        for (int i = 0; i < fileArray.size(); i++) {
            JSONObject fileObj = fileArray.getJSONObject(i);
            String pName = fileObj.getString("pName");
            String fileId = fileObj.getString("fileId");

            // Get VideoFile to read duration and update userId
            VideoFile videoFile = videoFileMapper.selectByFileId(fileId);
            int duration = 0;
            if (videoFile != null) {
                duration = videoFile.getDuration() != null ? videoFile.getDuration() : 0;
                videoFile.setUserId(token.getUserId());
                videoFileMapper.updateByFileId(videoFile);
            }
            totalDuration += duration;

            // Create VideoP record
            VideoP videoP = new VideoP();
            videoP.setPId(StringTools.getRandomNumber(Constants.LENGTH_10));
            videoP.setVideoId(videoId);
            videoP.setPName(pName);
            videoP.setFileId(fileId);
            videoP.setDuration(duration);
            videoP.setSort(i + 1);
            videoP.setCreateTime(new Date());
            videoPMapper.insert(videoP);
        }

        // Create Video record
        Video video = new Video();
        video.setVideoId(videoId);
        video.setUserId(token.getUserId());
        if (videoCover != null && !videoCover.isEmpty()) {
            video.setVideoCover(videoCover);
        } else {
            String firstFileId = fileArray.getJSONObject(0).getString("fileId");
            // 优先使用 merge 时预生成的封面
            VideoFile vf = videoFileMapper.selectByFileId(firstFileId);
            if (vf != null && vf.getVideoCover() != null && !vf.getVideoCover().isEmpty()) {
                video.setVideoCover(vf.getVideoCover());
            } else {
                String coverUrl = autoGenerateCover(firstFileId);
                if (coverUrl != null) {
                    video.setVideoCover(coverUrl);
                }
            }
        }
        video.setVideoName(videoName);
        video.setPCategoryId(pCategoryId);
        video.setCategoryId(categoryId);
        video.setPostType(postType);
        video.setTags(tags);
        video.setIntroduction(introduction);
        video.setInteraction(interaction);
        video.setDuration(totalDuration);
        video.setStatus(VideoStatusEnum.AUDITING.getCode());
        video.setPlayCount(0);
        video.setLikeCount(0);
        video.setCoinCount(0);
        video.setCollectCount(0);
        video.setCommentCount(0);
        video.setDanmuCount(0);
        video.setShareCount(0);
        video.setCreateTime(new Date());
        videoMapper.insert(video);
        return videoId;
    }

    @Override
    public PaginationResultVO<Video> loadVideoListByUserId(String userId, Integer status,
                                                           String videoNameFuzzy, Integer pageNo) {
        VideoQuery query = new VideoQuery();
        query.setUserId(userId);
        if (status != null) {
            query.setStatus(status);
        }
        if (videoNameFuzzy != null && !videoNameFuzzy.isEmpty()) {
            query.setVideoNameFuzzy(videoNameFuzzy);
        }

        Integer pageSize = 10;
        Integer pageNum = pageNo != null ? pageNo : 1;
        query.setPageNo(pageNum);
        query.setPageSize(pageSize);

        Long totalCount = videoMapper.selectCountByCondition(query);
        SimplePage simplePage = new SimplePage(pageNum, pageSize, totalCount);

        List<Video> list = videoMapper.selectListByCondition(query);
        // Enrich with user info for frontend display
        UserInfo user = userInfoMapper.selectByUserId(userId);
        if (user != null) {
            for (Video v : list) {
                v.setUserName(user.getNickName());
                v.setAvatar(user.getAvatar());
            }
        }
        return new PaginationResultVO<>(simplePage, list);
    }

    @Override
    public Video getVideoByVideoIdForUser(String videoId, String userId) {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            // 前端可能传的是 uploadId(UUID), 尝试通过 uploadId 找到 VideoFile 再转 videoId
            VideoFile videoFile = videoFileMapper.selectByUploadId(videoId);
            if (videoFile != null && videoFile.getFileId() != null) {
                video = videoMapper.selectByVideoId(videoFile.getFileId());
            }
        }
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        if (!Objects.equals(video.getUserId(), userId)) {
            throw new BusinessException("无权访问该视频");
        }
        return video;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveVideoInteraction(String videoId, String interaction, String userId) {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        if (!Objects.equals(video.getUserId(), userId)) {
            throw new BusinessException("无权操作该视频");
        }
        video.setInteraction(interaction);
        videoMapper.updateByVideoId(video);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideo(String videoId, String userId) {
        Video video = videoMapper.selectByVideoIdIncludeDeleted(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }
        if (video.getIsDeleted() != null && video.getIsDeleted() == 1) {
            // 视频已经删除，直接返回（幂等操作）
            return;
        }
        if (!Objects.equals(video.getUserId(), userId)) {
            throw new BusinessException("无权操作该视频");
        }
        videoMapper.deleteByVideoId(videoId);
    }

    /**
     * 获取用户视频的第一个文件ID
     */
    private String getFirstFileId(String videoId) {
        VideoPQuery vpQuery = new VideoPQuery();
        vpQuery.setVideoId(videoId);
        List<VideoP> vpList = videoPMapper.selectListByCondition(vpQuery);
        if (vpList != null && !vpList.isEmpty()) {
            return vpList.get(0).getFileId();
        }
        return null;
    }

    /**
     * 从视频第一帧自动生成封面并上传到OSS
     */
    private String autoGenerateCover(String fileId) {
        VideoFile videoFile = videoFileMapper.selectByFileId(fileId);
        if (videoFile == null) return null;

        String projectFolder = appconfig.getProjectFolder();
        String videoPath = projectFolder + "video/original/" + fileId + "/" + videoFile.getFileName();

        // 尝试原始文件
        File vidFile = new File(videoPath);
        if (!vidFile.exists()) {
            // 尝试转码后的MP4
            videoPath = projectFolder + "video/transcode/" + fileId + "/index.mp4";
            vidFile = new File(videoPath);
            if (!vidFile.exists()) {
                logger.warn("自动生成封面失败: 原文件不存在, fileId={}", fileId);
                return null;
            }
        }

        String tempDir = System.getProperty("java.io.tmpdir") + "/kilikili/cover/";
        new File(tempDir).mkdirs();
        String outputPath = tempDir + fileId + ".png";

        // 提取第1秒的帧
        boolean success = FFmpegUtils.extractFrame(videoPath, outputPath, "00:00:01");
        if (!success) {
            // 尝试第0秒
            success = FFmpegUtils.extractFrame(videoPath, outputPath, "00:00:00");
        }
        if (!success) {
            logger.warn("自动生成封面失败: FFmpeg提取帧失败, fileId={}", fileId);
            return null;
        }

        try {
            String coverUrl = videoFileService.uploadImage(outputPath, false);
            logger.info("自动生成的封面上传成功: {}", coverUrl);
            return coverUrl;
        } catch (Exception e) {
            logger.error("自动生成封面上传OSS失败: {}", e.getMessage());
            new File(outputPath).delete();
            return null;
        }
    }
}
