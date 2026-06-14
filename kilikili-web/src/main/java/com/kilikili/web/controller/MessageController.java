package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.po.Message;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.MessageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController("webMessageController")
@RequestMapping("/message")
@Validated
public class MessageController extends ABaseController {

    @Resource
    private MessageService messageService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/getNoReadCount")
    public ResponseVO getNoReadCount() {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        Long count = messageService.getNoReadCount(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(count);
    }

    @RequestMapping("/loadMessage")
    public ResponseVO loadMessage(@RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        PaginationResultVO<Message> result = messageService.loadMessage(tokenUserInfoDto.getUserId(), pageNo);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/delMessage")
    public ResponseVO delMessage(@NotEmpty String messageId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        messageService.delMessage(messageId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getNoReadCountGroup")
    public ResponseVO getNoReadCountGroup() {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        List<Map<String, Object>> result = messageService.getNoReadCountGroup(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/readAll")
    public ResponseVO readAll() {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        messageService.readAll(tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
