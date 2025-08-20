package com.example.videoapp.service;

import com.example.videoapp.DTO.NotificationMessageDTO;
import com.example.videoapp.config.RabbitMQConfig;
import com.example.videoapp.entity.Notification;
import com.example.videoapp.entity.User;
import com.example.videoapp.mapper.NotificationMapper;
import com.example.videoapp.mapper.UserMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 通知服务，处理用户通知的创建、获取和状态管理
 */
@Service
public class NotificationService {
    
    @Autowired
    private NotificationMapper notificationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 监听RabbitMQ队列，处理通知消息
     * 
     * @param notificationMessageDTO 通知消息DTO
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE_NAME)
    public void handleNotification(NotificationMessageDTO notificationMessageDTO) {
        Notification notification = new Notification();
        notification.setSenderId(notificationMessageDTO.getSenderId());
        notification.setRecipientId(notificationMessageDTO.getRecipientId());
        notification.setType(notificationMessageDTO.getType());
        
        String senderName = notificationMessageDTO.getSenderName();
        String videoTitle = notificationMessageDTO.getVideoTitle();
        
        // 根据通知类型生成不同的通知内容
        if (notification.getType().equals("NEW_COMMENT")) {
            notification.setContent("用户" + senderName + "评论了您的视频:" + videoTitle);
        } else if (notification.getType().equals("NEW_FISH")) {
            notification.setContent("您的视频:" + videoTitle + "收到了用户" + senderName + "的小鱼！");
        }
        
        notification.setRelatedEntityId(notificationMessageDTO.getVideoId());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        notificationMapper.insert(notification);
    }

    /**
     * 获取指定用户的通知列表
     * 
     * @param username 用户名
     * @return 通知列表
     */
    public List<Notification> getNotificationsForUser(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            return Collections.emptyList();
        }
        return notificationMapper.findByRecipientId(user.getId());
    }

    /**
     * 将指定通知标记为已读
     * 
     * @param notificationId 通知ID
     * @param username 操作用户名
     */
    @Transactional
    public void markNotificationAsRead(Long notificationId, String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        Notification notification = notificationMapper.findById(notificationId);
        if (notification == null) {
            throw new RuntimeException("找不到ID为 " + notificationId + " 的通知");
        }
        
        if (!notification.getRecipientId().equals(user.getId())) {
            throw new RuntimeException("当前用户和信息接收者不一致");
        }
        
        notification.setRead(true);
        notificationMapper.update(notification);
    }
    
    /**
     * 将用户的所有通知标记为已读
     * 
     * @param username 用户名
     */
    @Transactional
    public void markAllAsReadForUser(String username) {
        User user = userMapper.findByUsername(username);
        if (user != null) {
            notificationMapper.markAllAsReadByRecipientId(user.getId());
        }
    }
}
