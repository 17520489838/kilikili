package com.kilikili.web.controller;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.ResponseCodeEnum;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.service.CommentService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController("webCommentController")
@RequestMapping("/comment")
@Validated
public class CommentController extends ABaseController {

    @Resource
    private CommentService commentService;

    @Resource
    private RedisComponent redisComponent;

    @RequestMapping("/postComment")
    public ResponseVO postComment(@NotEmpty String videoId, @NotEmpty String content,
                                  String replyCommentId, String imgPath) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        commentService.postComment(tokenUserInfoDto, videoId, content, replyCommentId, imgPath);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(@NotEmpty String videoId, Integer pageNo, Integer orderType) {
        return getSuccessResponseVO(commentService.loadComment(
                videoId, pageNo != null ? pageNo : 1, orderType));
    }

    @RequestMapping("/likeComment")
    public ResponseVO likeComment(@NotEmpty String commentId, @NotEmpty String videoId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        commentService.likeComment(tokenUserInfoDto, commentId, videoId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadReply")
    public ResponseVO loadReply(@NotEmpty String videoId, @NotEmpty String commentId, Integer pageNo) {
        return getSuccessResponseVO(commentService.loadReply(
                videoId, commentId, pageNo != null ? pageNo : 1));
    }

    @RequestMapping("/topComment")
    public ResponseVO topComment(@NotEmpty String commentId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        commentService.topComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/cancelTopComment")
    public ResponseVO cancelTopComment(@NotEmpty String commentId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        commentService.cancelTopComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/userDelComment")
    public ResponseVO userDelComment(@NotEmpty String commentId) {
        String token = getTokenFromCookie();
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            throw new BusinessException(ResponseCodeEnum.UNAUTHORIZED);
        }
        commentService.userDelComment(commentId, tokenUserInfoDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
