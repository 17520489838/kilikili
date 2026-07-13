package com.kilikili.service;

import com.kilikili.entity.constants.Constants;
import com.kilikili.entity.po.Danmu;
import com.kilikili.mappers.CommentMapper;
import com.kilikili.mappers.DanmuMapper;
import com.kilikili.redis.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

@Service("aiAuditService")
public class AIAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AIAuditService.class);
    private static final String REDIS_KEY_DANMU = Constants.REDIS_KEY_PREFIX + "danmu:";

    @Resource
    private AIService aiService;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private DanmuMapper danmuMapper;
    @Resource
    private RedisUtils<Object> redisUtils;
    @Resource
    @Qualifier("aiExecutor")
    private Executor aiExecutor;

    @Value("${ai.audit.enabled:false}")
    private Boolean aiAuditEnabled;

    public void auditCommentAsync(String commentId, String content) {
        if (!Boolean.TRUE.equals(aiAuditEnabled)) {
            return;
        }
        aiExecutor.execute(() -> {
            try {
                Integer auditStatus = aiService.auditText(content);
                if (auditStatus == null || auditStatus == 0) {
                    return;
                }
                commentMapper.updateAuditStatus(commentId, auditStatus);
                if (auditStatus == 3) {
                    commentMapper.updateStatus(commentId, 2);
                    logger.info("AI审核屏蔽违规评论, commentId={}", commentId);
                }
            } catch (Throwable t) {
                logger.warn("AI审核评论失败, commentId={}", commentId, t);
            }
        });
    }

    public void auditDanmuAsync(String danmuId, String content) {
        if (!Boolean.TRUE.equals(aiAuditEnabled)) {
            return;
        }
        aiExecutor.execute(() -> {
            try {
                Integer auditStatus = aiService.auditText(content);
                if (auditStatus == null || auditStatus == 0) {
                    return;
                }
                danmuMapper.updateAuditStatus(danmuId, auditStatus);
                if (auditStatus == 3) {
                    // Query danmu info BEFORE marking deleted (selectByDanmuId filters is_deleted=0)
                    Danmu danmu = danmuMapper.selectByDanmuId(danmuId);
                    if (danmu != null) {
                        String cacheKey = REDIS_KEY_DANMU + danmu.getFileId() + "_" + danmu.getVideoId();
                        redisUtils.delete(cacheKey);
                    }

                    Danmu d = new Danmu();
                    d.setDanmuId(danmuId);
                    d.setIsDeleted(1);
                    danmuMapper.updateByDanmuId(d);
                    logger.info("AI审核屏蔽违规弹幕, danmuId={}", danmuId);
                }
            } catch (Throwable t) {
                logger.warn("AI审核弹幕失败, danmuId={}", danmuId, t);
            }
        });
    }
}
