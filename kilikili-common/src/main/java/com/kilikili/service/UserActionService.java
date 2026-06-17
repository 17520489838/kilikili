package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.UserAction;

public interface UserActionService {
    void doAction(TokenUserInfoDto token, String videoId, Integer actionType, Integer actionCount, String commentId);
    UserAction getUserAction(String userId, String videoId, Integer actionType);
}
