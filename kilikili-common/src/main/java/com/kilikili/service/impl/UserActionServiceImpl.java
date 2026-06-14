package com.kilikili.service.impl;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.MessageTypeEnum;
import com.kilikili.entity.enums.UserActionTypeEnum;
import com.kilikili.entity.po.UserAction;
import com.kilikili.entity.po.Video;
import com.kilikili.mappers.UserActionMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.redis.RedisUtils;
import com.kilikili.service.MessageService;
import com.kilikili.service.UserActionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

@Service("userActionService")
public class UserActionServiceImpl implements UserActionService {

    @Resource
    private UserActionMapper userActionMapper;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private MessageService messageService;
    @Resource
    private RedisUtils<Object> redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAction(TokenUserInfoDto token, String videoId, Integer actionType,
                         Integer actionCount, String commentId) {
        // Check if user already performed this action
        UserAction existingAction = userActionMapper.selectByUserVideoAction(
                token.getUserId(), videoId, actionType);

        if (existingAction != null) {
            // Action already exists -> undo (remove action)
            existingAction.setIsDeleted(1);
            userActionMapper.updateByUserVideoAction(existingAction);

            // Decrement video counter
            String countField = getCountFieldByActionType(actionType);
            if (countField != null) {
                videoMapper.updateCount(videoId, countField, -1);
            }
        } else {
            // Action does not exist -> add action
            UserAction action = new UserAction();
            action.setUserId(token.getUserId());
            action.setVideoId(videoId);
            action.setActionType(actionType);
            action.setActionCount(actionCount != null ? actionCount : 1);
            action.setCommentId(commentId);
            action.setLastActionTime(new Date());
            action.setCreateTime(new Date());
            userActionMapper.insert(action);

            // Increment video counter
            String countField = getCountFieldByActionType(actionType);
            if (countField != null) {
                videoMapper.updateCount(videoId, countField, 1);
            }

            // Send notification for like (0) and collect (1) actions to video owner
            Video video = videoMapper.selectByVideoId(videoId);
            if (video != null && !video.getUserId().equals(token.getUserId())) {
                Integer messageType = null;
                if (UserActionTypeEnum.LIKE.getCode().equals(actionType)) {
                    messageType = MessageTypeEnum.LIKE.getCode();
                } else if (UserActionTypeEnum.COLLECT.getCode().equals(actionType)) {
                    messageType = MessageTypeEnum.COLLECT.getCode();
                }
                if (messageType != null) {
                    messageService.addMessage(video.getUserId(), token.getUserId(),
                            messageType, "", videoId);
                }
            }
        }
    }

    /**
     * Map action type to the corresponding count field name in video table
     */
    private String getCountFieldByActionType(Integer actionType) {
        if (UserActionTypeEnum.LIKE.getCode().equals(actionType)) {
            return "like_count";
        } else if (UserActionTypeEnum.COLLECT.getCode().equals(actionType)) {
            return "collect_count";
        } else if (UserActionTypeEnum.COIN.getCode().equals(actionType)) {
            return "coin_count";
        }
        return null;
    }
}
