package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.po.Danmu;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.DanmuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("webDanmuController")
@RequestMapping("/danmu")
@Validated
public class DanmuController extends ABaseController {

    @Resource
    private DanmuService danmuService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/postDanmu")
    public ResponseVO postDanmu(@NotEmpty String videoId, @NotEmpty String fileId,
                                @NotEmpty String text, @NotNull Integer mode,
                                String color, @NotNull Integer time) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        danmuService.postDanmu(tokenUserInfoDto, videoId, fileId, text, mode, color, time);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(@NotEmpty String fileId, @NotEmpty String videoId) {
        List<Danmu> result = danmuService.loadDanmu(fileId, videoId);
        return getSuccessResponseVO(result);
    }
}
