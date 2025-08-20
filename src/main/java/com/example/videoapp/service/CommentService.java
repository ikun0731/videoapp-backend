package com.example.videoapp.service;

import com.example.videoapp.DTO.CommentDetailDTO;
import com.example.videoapp.DTO.NotificationMessageDTO;
import com.example.videoapp.config.RabbitMQConfig;
import com.example.videoapp.entity.Comment;
import com.example.videoapp.entity.User;
import com.example.videoapp.entity.Video;
import com.example.videoapp.mapper.CommentMapper;
import com.example.videoapp.mapper.UserMapper;
import com.example.videoapp.mapper.VideoMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论服务，处理视频评论相关的业务逻辑
 */
@Service
public class CommentService {
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    /**
     * 获取视频的所有评论
     * 
     * @param videoId 视频ID
     * @return 评论列表
     */
    public List<CommentDetailDTO> getCommentsByVideoId(Long videoId) {
        return commentMapper.findByVideoId(videoId);
    }
    
    /**
     * 创建新评论
     * 
     * @param videoId 视频ID
     * @param content 评论内容
     * @param username 评论用户名
     * @return 创建的评论对象
     */
    public Comment createComment(Long videoId, String content, String username) {
        // 查找评论用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 创建评论
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setContent(content);
        comment.setUserId(user.getId());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        // 发送评论通知
        Video video = videoMapper.findById(videoId);
        if (video != null) {
            // 不给自己的视频发送通知
            if (!user.getId().equals(video.getUserId())) {
                NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO(
                        "NEW_COMMENT",
                        user.getId(),
                        video.getUserId(),
                        videoId,
                        user.getNickname(),
                        video.getTitle());
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.NOTIFICATION_EXCHANGE_NAME,
                        RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                        notificationMessageDTO);
            }
        }
        
        return comment;
    }
    
    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @param username 操作用户名
     */
    public void deleteComment(Long commentId, String username) {
        // 查找评论
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            throw new RuntimeException("无法找到评论");
        }
        
        // 检查权限
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!comment.getUserId().equals(user.getId())) {
            throw new RuntimeException("无权删除他人评论");
        }
        
        // 删除评论
        commentMapper.deleteById(commentId);
    }
}
