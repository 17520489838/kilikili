package com.kilikili.service.impl;

import com.kilikili.entity.dto.TokenUserInfoDto;
import com.kilikili.entity.enums.MessageTypeEnum;
import com.kilikili.entity.enums.UserActionTypeEnum;
import com.kilikili.entity.po.Comment;
import com.kilikili.entity.po.UserAction;
import com.kilikili.entity.po.UserInfo;
import com.kilikili.entity.po.Video;
import com.kilikili.entity.query.CommentQuery;
import com.kilikili.entity.query.SimplePage;
import com.kilikili.entity.vo.PaginationResultVO;
import com.kilikili.exception.BusinessException;
import com.kilikili.mappers.CommentMapper;
import com.kilikili.mappers.UserActionMapper;
import com.kilikili.mappers.UserInfoMapper;
import com.kilikili.mappers.VideoMapper;
import com.kilikili.service.CommentService;
import com.kilikili.service.MessageService;
import com.kilikili.utils.StringTools;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.kilikili.entity.constants.Constants.LENGTH_10;
import static com.kilikili.entity.constants.Constants.LENGTH_5;

@Service("commentService")
public class CommentServiceImpl implements CommentService {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private VideoMapper videoMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private MessageService messageService;
    @Resource
    private UserActionMapper userActionMapper;

    /**
     * Enrich a list of comments with user info (nickName, avatar)
     */
    private void enrichComments(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) return;
        Set<String> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;

        // Batch query user info
        Map<String, UserInfo> userMap = new HashMap<>();
        for (String uid : userIds) {
            UserInfo user = userInfoMapper.selectByUserId(uid);
            if (user != null) userMap.put(uid, user);
        }

        for (Comment c : comments) {
            UserInfo user = userMap.get(c.getUserId());
            if (user != null) {
                c.setNickName(user.getNickName());
                c.setAvatar(user.getAvatar());
            }
        }
    }

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
    public PaginationResultVO<Map<String, Object>> loadComment(String videoId, Integer pageNo, Integer orderType) {
        CommentQuery query = new CommentQuery();
        query.setVideoId(videoId);
        query.setPageNo(pageNo != null ? pageNo : 1);
        query.setPageSize(20);

        // Filter top-level comments only (p_comment_id IS NULL)
        query.setPCommentIdNull(true);

        // orderType: 0 = time desc, 1 = hot (like_count desc)
        if (orderType != null && orderType == 1) {
            query.setOrderBy("top_type DESC, like_count");
            query.setOrderDirection("desc");
        } else {
            query.setOrderBy("top_type DESC, create_time");
            query.setOrderDirection("desc");
        }

        Long totalCount = commentMapper.selectCountByCondition(query);
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize(), totalCount);

        List<Comment> topComments = commentMapper.selectListByCondition(query);

        // Enrich top-level comments with user info
        enrichComments(topComments);

        // Batch load replies for all top-level comments
        if (!topComments.isEmpty()) {
            List<String> parentIds = topComments.stream()
                    .map(Comment::getCommentId)
                    .collect(Collectors.toList());

            CommentQuery replyQuery = new CommentQuery();
            replyQuery.setVideoId(videoId);
            replyQuery.setPageNo(1);
            replyQuery.setPageSize(200); // Load up to 200 replies total for the page
            replyQuery.setOrderBy("create_time");
            replyQuery.setOrderDirection("asc");

            List<Comment> allReplies = new ArrayList<>();
            for (String parentId : parentIds) {
                CommentQuery pq = new CommentQuery();
                pq.setPCommentId(parentId);
                pq.setPageNo(null);
                pq.setPageSize(null);
                pq.setOrderBy("create_time");
                pq.setOrderDirection("asc");
                List<Comment> replies = commentMapper.selectListByCondition(pq);
                if (replies != null && !replies.isEmpty()) {
                    enrichComments(replies);
                    allReplies.addAll(replies);
                }
            }

            // Group replies by parent commentId
            Map<String, List<Comment>> replyMap = allReplies.stream()
                    .filter(c -> c.getPCommentId() != null)
                    .collect(Collectors.groupingBy(Comment::getPCommentId));

            // Build result list with nested structure
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Comment top : topComments) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("commentId", top.getCommentId());
                item.put("videoId", top.getVideoId());
                item.put("userId", top.getUserId());
                item.put("content", top.getContent());
                item.put("imgPath", top.getImgPath());
                item.put("replyCommentId", top.getPCommentId());
                item.put("replyUserId", top.getReplyUserId());
                item.put("nickName", top.getNickName());
                item.put("avatar", top.getAvatar());
                item.put("likeCount", top.getLikeCount());
                item.put("topType", top.getTopType());
                item.put("postTime", top.getCreateTime() != null ? SDF.format(top.getCreateTime()) : "");

                // Attach replies as children
                List<Comment> replies = replyMap.get(top.getCommentId());
                List<Map<String, Object>> childrenList = new ArrayList<>();
                if (replies != null) {
                    for (Comment reply : replies) {
                        Map<String, Object> child = new LinkedHashMap<>();
                        child.put("commentId", reply.getCommentId());
                        child.put("videoId", reply.getVideoId());
                        child.put("userId", reply.getUserId());
                        child.put("content", reply.getContent());
                        child.put("imgPath", reply.getImgPath());
                        child.put("replyCommentId", reply.getPCommentId());
                        child.put("replyUserId", reply.getReplyUserId());
                        child.put("nickName", reply.getNickName());
                        child.put("avatar", reply.getAvatar());
                        child.put("likeCount", reply.getLikeCount());
                        child.put("topType", reply.getTopType());
                        child.put("postTime", reply.getCreateTime() != null ? SDF.format(reply.getCreateTime()) : "");
                        childrenList.add(child);
                    }
                }
                item.put("children", childrenList);

                resultList.add(item);
            }

            return new PaginationResultVO<>(simplePage, resultList);
        }

        // No replies: convert top comments to map format
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Comment top : topComments) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commentId", top.getCommentId());
            item.put("videoId", top.getVideoId());
            item.put("userId", top.getUserId());
            item.put("content", top.getContent());
            item.put("imgPath", top.getImgPath());
            item.put("replyCommentId", top.getPCommentId());
            item.put("replyUserId", top.getReplyUserId());
            item.put("nickName", top.getNickName());
            item.put("avatar", top.getAvatar());
            item.put("likeCount", top.getLikeCount());
            item.put("topType", top.getTopType());
            item.put("postTime", top.getCreateTime() != null ? SDF.format(top.getCreateTime()) : "");
            item.put("children", new ArrayList<>());
            resultList.add(item);
        }

        return new PaginationResultVO<>(simplePage, resultList);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(TokenUserInfoDto token, String commentId, String videoId) {
        // Check existing action
        UserAction existingAction = userActionMapper.selectByUserCommentAction(
                token.getUserId(), commentId, UserActionTypeEnum.LIKE.getCode());

        if (existingAction != null && existingAction.getIsDeleted() == 0) {
            // Unlike: soft delete
            existingAction.setIsDeleted(1);
            existingAction.setUpdateTime(new Date());
            userActionMapper.updateByUserVideoAction(existingAction);
            commentMapper.updateLikeCount(commentId, -1);
        } else if (existingAction != null && existingAction.getIsDeleted() == 1) {
            // Re-like
            existingAction.setIsDeleted(0);
            existingAction.setLastActionTime(new Date());
            existingAction.setUpdateTime(new Date());
            userActionMapper.updateByUserVideoAction(existingAction);
            commentMapper.updateLikeCount(commentId, 1);
        } else {
            // New like
            UserAction action = new UserAction();
            action.setUserId(token.getUserId());
            action.setVideoId(videoId);
            action.setCommentId(commentId);
            action.setActionType(UserActionTypeEnum.LIKE.getCode());
            action.setActionCount(1);
            action.setLastActionTime(new Date());
            action.setCreateTime(new Date());
            userActionMapper.insert(action);
            commentMapper.updateLikeCount(commentId, 1);
        }
    }

    @Override
    public PaginationResultVO<Map<String, Object>> loadReply(String videoId, String commentId, Integer pageNo) {
        CommentQuery query = new CommentQuery();
        query.setVideoId(videoId);
        query.setPCommentId(commentId);
        query.setPageNo(pageNo != null ? pageNo : 1);
        query.setPageSize(20);
        query.setOrderBy("create_time");
        query.setOrderDirection("asc");

        Long totalCount = commentMapper.selectCountByCondition(query);
        SimplePage simplePage = new SimplePage(query.getPageNo(), query.getPageSize(), totalCount);
        List<Comment> replies = commentMapper.selectListByCondition(query);
        enrichComments(replies);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Comment reply : replies) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commentId", reply.getCommentId());
            item.put("videoId", reply.getVideoId());
            item.put("userId", reply.getUserId());
            item.put("content", reply.getContent());
            item.put("imgPath", reply.getImgPath());
            item.put("replyCommentId", reply.getPCommentId());
            item.put("replyUserId", reply.getReplyUserId());
            item.put("nickName", reply.getNickName());
            item.put("avatar", reply.getAvatar());
            item.put("likeCount", reply.getLikeCount());
            item.put("topType", reply.getTopType());
            item.put("postTime", reply.getCreateTime() != null ? SDF.format(reply.getCreateTime()) : "");
            resultList.add(item);
        }
        return new PaginationResultVO<>(simplePage, resultList);
    }
}
