package com.kilikili.service.impl;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.po.UserFocus;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.query.UserFocusQuery;
import com.kilikili.entity.query.UserInfoQuery;
import com.kilikili.exception.BusinessException;
import com.kilikili.mappers.UserFocusMapper;
import com.kilikili.mappers.UserInfoMapper;
import com.kilikili.service.UserFocusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("userFocusService")
public class UserFocusServiceImpl implements UserFocusService {

    @Resource
    private UserFocusMapper userFocusMapper;
    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void focus(TokenUserInfoDto token, String focusUserId) {
        if (token.getUserId().equals(focusUserId)) {
            throw new BusinessException("不能关注自己");
        }
        UserFocus exist = userFocusMapper.selectByUserFocus(token.getUserId(), focusUserId);
        if (exist != null) {
            throw new BusinessException("已关注该用户");
        }
        UserFocus userFocus = new UserFocus();
        userFocus.setUserId(token.getUserId());
        userFocus.setFocusUserId(focusUserId);
        userFocus.setCreateTime(new Date());
        userFocus.setUpdateTime(new Date());
        userFocusMapper.insert(userFocus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelFocus(TokenUserInfoDto token, String focusUserId) {
        userFocusMapper.deleteByUserFocus(token.getUserId(), focusUserId);
    }

    @Override
    public List<UserInfo> loadFocusList(String userId) {
        UserFocusQuery query = new UserFocusQuery();
        query.setUserId(userId);
        query.setPageNo(null);
        query.setPageSize(null);
        List<UserFocus> focusList = userFocusMapper.selectListByCondition(query);
        if (focusList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> focusUserIds = focusList.stream()
                .map(UserFocus::getFocusUserId)
                .collect(Collectors.toList());
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setUserIdList(focusUserIds);
        return userInfoMapper.selectListByCondition(userInfoQuery);
    }

    @Override
    public List<UserInfo> loadFansList(String userId) {
        UserFocusQuery query = new UserFocusQuery();
        query.setFocusUserId(userId);
        query.setPageNo(null);
        query.setPageSize(null);
        List<UserFocus> fansList = userFocusMapper.selectListByCondition(query);
        if (fansList.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> userIds = fansList.stream()
                .map(UserFocus::getUserId)
                .collect(Collectors.toList());
        UserInfoQuery userInfoQuery = new UserInfoQuery();
        userInfoQuery.setUserIdList(userIds);
        return userInfoMapper.selectListByCondition(userInfoQuery);
    }

    @Override
    public Boolean isFocus(String userId, String focusUserId) {
        UserFocus userFocus = userFocusMapper.selectByUserFocus(userId, focusUserId);
        return userFocus != null;
    }
}
