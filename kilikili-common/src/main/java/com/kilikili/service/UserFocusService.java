package com.kilikili.service;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.UserInfo;

import java.util.List;

public interface UserFocusService {
    void focus(TokenUserInfoDto token, String focusUserId);
    void cancelFocus(TokenUserInfoDto token, String focusUserId);
    List<UserInfo> loadFocusList(String userId);
    List<UserInfo> loadFansList(String userId);
    Boolean isFocus(String userId, String focusUserId);
}
