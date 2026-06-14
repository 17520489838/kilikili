package com.kilikili.service.impl;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.MessageTypeEnum;
import com.kilikili.entity.po.Comment;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.CommentQuery;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.mappers.CommentMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.service.CommentService;
import com.kilikili.service.MessageService;
import com.kilikili.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static com.kilikili.entity.constants.Constants.LENGTH_10;
import static com.kilikili.entity.constants.Constants.LENGTH_5;

@Service("commentService")
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private MessageService messageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postComment(TokenUserInfoDto token, String videoId, String content,
                            String replyCommentId, String imgPath) {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new BusinessException("视频不存在");
        }

        Comment comment = new Comment();
        comment.setCommentId(StringTools.getRandomNumber(LENGTH_10));
        comment.setVideoId(videoId);
        comment.setUserId(token.getUserId());
        comment.setContent(content);
        comment.setImgPath(imgPath);
        comment.setStatus(1); // audit pass
        comment.setLikeCount(0);
        comment.setTopType(0);

        if (replyCommentId != null && !replyCommentId.isEmpty()) {
            comment.setPCommentId(replyCommentId);
            Comment parentComment = commentMapper.selectByCommentId(replyCommentId);
            if (parentComment != null) {
                comment.setReplyUserId(parentComment.getUserId());
            }
        }
        comment.setCreateTime(new Date());
        commentMapper.insert(comment);

        // Increment video comment_count
        video.setCommentCount(video.getCommentCount() != null ? video.getCommentCount() + 1 : 1);
        videoMapper.updateByVideoId(video);

        // Add message to video owner if not self-comment
        if (!video.getUserId().equals(token.getUserId())) {
            messageService.addMessage(video.getUserId(), token.getUserId(),
                    MessageTypeEnum.COMMENT.getCode(), content, videoId);
        }
    }

    @Override
    public PaginationResultVO<Comment> loadComment(String videoId, Integer pageNo, Integer orderType) {
        CommentQuery query = new CommentQuery();
        query.setVideoId(videoId);
        query.setPageNo(pageNo != null ? pageNo : 1);
        query.setPageSize(20);

        // orderType: 0 = time desc, 1 = hot (like_count desc)
        // Top comments (top_type=1) always come first
        if (orderType != null && orderType == 1) {
            query.setOrderBy("top_type DESC, like_count");
            query.setOrderDirection("desc");
        } else {
            query.setOrderBy("top_type DESC, create_time");
            query.setOrderDirection("desc");
        }

        Long totalCount = commentMapper.selectCountByCondition(query);
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize(), totalCount);

        List<Comment> list = commentMapper.selectListByCondition(query);
        return new PaginationResultVO<>(simplePage, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void topComment(String commentId, String userId) {
        Comment comment = commentMapper.selectByCommentId(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        Video video = videoMapper.selectByVideoId(comment.getVideoId());
        if (video == null || !video.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        comment.setTopType(1);
        commentMapper.updateByCommentId(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTopComment(String commentId, String userId) {
        Comment comment = commentMapper.selectByCommentId(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        Video video = videoMapper.selectByVideoId(comment.getVideoId());
        if (video == null || !video.getUserId().equals(userId)) {
            throw new BusinessException("无权操作");
        }
        comment.setTopType(0);
        commentMapper.updateByCommentId(comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userDelComment(String commentId, String userId) {
        Comment comment = commentMapper.selectByCommentId(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        comment.setStatus(2);
        commentMapper.updateByCommentId(comment);
    }

    @Override
    public PaginationResultVO<Comment> loadCommentPage(CommentQuery query) {
        if (query.getPageNo() == null) query.setPageNo(1);
        if (query.getPageSize() == null) query.setPageSize(20);

        Long totalCount = commentMapper.selectCountByCondition(query);
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize(), totalCount);

        List<Comment> list = commentMapper.selectListByCondition(query);
        return new PaginationResultVO<>(simplePage, list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delCommentByAdmin(String commentId) {
        commentMapper.deleteByCommentId(commentId);
    }
}
