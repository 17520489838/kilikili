package com.kilikili.admin.controller;

import com.kilikili.entity.query.CommentQuery;
import com.kilikili.entity.vo.ResponseVO;
import com.kilikili.service.CommentService;
import com.kilikili.service.DanmuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController("adminInteractController")
@RequestMapping("/interact")
@Validated
public class InteractController extends ABaseController {

    @Resource
    private DanmuService danmuService;

    @Resource
    private CommentService commentService;

    @RequestMapping("/loadDanmu")
    public ResponseVO loadDanmu(Integer pageNo, Integer pageSize, String videoId, String textFuzzy) {
        return getSuccessResponseVO(danmuService.loadDanmuByPage(pageNo, pageSize, videoId, textFuzzy));
    }

    @RequestMapping("/delDanmu")
    public ResponseVO delDanmu(@NotEmpty String danmuId) {
        danmuService.delDanmu(danmuId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadComment")
    public ResponseVO loadComment(Integer pageNo, Integer pageSize, String videoId) {
        CommentQuery query = new CommentQuery();
        query.setPageNo(pageNo != null ? pageNo : 1);
        query.setPageSize(pageSize != null ? pageSize : 20);
        query.setVideoId(videoId);
        return getSuccessResponseVO(commentService.loadCommentPage(query));
    }

    @RequestMapping("/delComment")
    public ResponseVO delComment(@NotEmpty String commentId) {
        commentService.delCommentByAdmin(commentId);
        return getSuccessResponseVO(null);
    }
}
