package com.kilikili.service.impl;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.MessageTypeEnum;
import com.kilikili.entity.enums.UserActionTypeEnum;
import com.kilikili.entity.po.UserAction;
import com.kilikili.exception.BusinessException;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.po.Video;
import com.kilikili.mappers.UserActionMapper;
import com.kilikili.mappers.UserInfoMapper;
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
    private UserInfoMapper userInfoMapper;
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
            // Coins are one-way consumption, cannot be undone
            if (UserActionTypeEnum.COIN.getCode().equals(actionType)) {
                throw new BusinessException("已投过币，不能重复投币");
            }
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

            // Coin logic: balance check, deduct from user, credit to creator
            if (UserActionTypeEnum.COIN.getCode().equals(actionType)) {
                int coinAmount = action.getActionCount() != null ? action.getActionCount() : 1;
                if (coinAmount < 1 || coinAmount > 2) {
                    throw new BusinessException("投币数量只能为1或2");
                }

                UserInfo currentUser = userInfoMapper.selectByUserId(token.getUserId());
                if (currentUser == null || currentUser.getCurrentCoinCount() == null
                        || currentUser.getCurrentCoinCount() < coinAmount) {
                    throw new BusinessException("硬币不足");
                }

                // Deduct from current user
                currentUser.setCurrentCoinCount(currentUser.getCurrentCoinCount() - coinAmount);
                userInfoMapper.updateByUserId(currentUser);

                // Credit to video creator (if not self-coin)
                if (video != null && !video.getUserId().equals(token.getUserId())) {
                    UserInfo creator = userInfoMapper.selectByUserId(video.getUserId());
                    if (creator != null) {
                        creator.setCurrentCoinCount(
                                (creator.getCurrentCoinCount() != null ? creator.getCurrentCoinCount() : 0) + coinAmount);
                        userInfoMapper.updateByUserId(creator);
                    }

                    // Send coin notification
                    messageService.addMessage(video.getUserId(), token.getUserId(),
                            MessageTypeEnum.COIN.getCode(), "", videoId);
                }
            }
        }
    }

    @Override
    public UserAction getUserAction(String userId, String videoId, Integer actionType) {
        return userActionMapper.selectByUserVideoAction(userId, videoId, actionType);
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
