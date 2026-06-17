package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.vo.PaginationResultVO;

import java.util.Map;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.PlayHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController("webHistoryController")
@RequestMapping("/history")
@Validated
public class HistoryController extends ABaseController {

    @Resource
    private PlayHistoryService playHistoryService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/loadHistory")
    public ResponseVO loadHistory(@RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        PaginationResultVO<Map<String, Object>> result = playHistoryService.loadHistory(tokenUserInfoDto.getUserId(), pageNo);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/delHistory")
    public ResponseVO delHistory(@NotEmpty String videoId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        playHistoryService.delHistory(tokenUserInfoDto.getUserId(), videoId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/cleanHistory")
    public ResponseVO cleanHistory() {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        playHistoryService.cleanHistory(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
