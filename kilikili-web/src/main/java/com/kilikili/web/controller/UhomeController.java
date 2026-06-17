package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.enums.VideoStatusEnum;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.UserCollectionService;
import com.kilikili.service.UserFocusService;
import com.kilikili.service.UserInfoService;
import com.kilikili.service.VideoService;
import com.kilikili.utils.StringTools;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController("webUhomeController")
@RequestMapping("/uhome")
@Validated
public class UhomeController extends ABaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private VideoService videoService;

    @Resource
    private UserFocusService userFocusService;

    @Resource
    private UserCollectionService userCollectionService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/updateUserInfo")
    public ResponseVO updateUserInfo(String nickName, String avatar, String birthday,
                                     String school, String personIntroduction, String noticeInfo) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        if (nickName != null) {
            userInfo.setNickName(nickName);
        }
        if (avatar != null) {
            userInfo.setAvatar(avatar);
        }
        if (birthday != null) {
            userInfo.setBirthday(birthday);
        }
        if (school != null) {
            userInfo.setSchool(school);
        }
        if (personIntroduction != null) {
            userInfo.setPersonIntroduction(personIntroduction);
        }
        if (noticeInfo != null) {
            userInfo.setNoticeInfo(noticeInfo);
        }
        userInfoService.updateByUserId(userInfo);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadVideoList")
    public ResponseVO loadVideoList(@NotEmpty String userId, Integer pageNo) {
        return getSuccessResponseVO(videoService.loadVideoListByUserId(
                userId, VideoStatusEnum.PUBLISHED.getCode(), null, pageNo));
    }

    @RequestMapping("/getUserInfo")
    public ResponseVO getUserInfo(@NotEmpty String userId) {
        return getSuccessResponseVO(userInfoService.getUserInfoByUserId(userId));
    }

    @RequestMapping("/focus")
    public ResponseVO focus(@NotEmpty String focusUserId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        userFocusService.focus(tokenUserInfoDto, focusUserId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/cancelFocus")
    public ResponseVO cancelFocus(@NotEmpty String focusUserId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        userFocusService.cancelFocus(tokenUserInfoDto, focusUserId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadFocusList")
    public ResponseVO loadFocusList(String userId) {
        if (StringTools.isEmpty(userId)) {
            String token = getTokenFromCookie();
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
            if (tokenUserInfoDto == null) {
                throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
            }
            userId = tokenUserInfoDto.getUserId();
        }
        return getSuccessResponseVO(userFocusService.loadFocusList(userId));
    }

    @RequestMapping("/loadFansList")
    public ResponseVO loadFansList(String userId) {
        if (StringTools.isEmpty(userId)) {
            String token = getTokenFromCookie();
            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
            if (tokenUserInfoDto == null) {
                throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
            }
            userId = tokenUserInfoDto.getUserId();
        }
        return getSuccessResponseVO(userFocusService.loadFansList(userId));
    }

    @RequestMapping("/loadUserCollection")
    public ResponseVO loadUserCollection(Integer pageNo) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        return getSuccessResponseVO(userCollectionService.loadUserCollection(
                tokenUserInfoDto.getUserId(), pageNo));
    }

    @RequestMapping("/getCoinBalance")
    public ResponseVO getCoinBalance() {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(userInfo != null ? userInfo.getCurrentCoinCount() : 0);
    }

    @RequestMapping("/saveTheme")
    public ResponseVO saveTheme(Integer theme) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
        if (userInfo == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        userInfo.setTheme(theme);
        userInfoService.updateByUserId(userInfo);
        return getSuccessResponseVO(null);
    }
}
