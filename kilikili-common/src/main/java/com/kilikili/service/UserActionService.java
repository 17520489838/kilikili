package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;

public interface UserActionService {
    void doAction(TokenUserInfoDto token, String videoId, Integer actionType, Integer actionCount, String commentId);
}
