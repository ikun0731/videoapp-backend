package com.example.videoapp.controller;

import com.example.videoapp.common.Result;
import com.example.videoapp.entity.Notification;
import com.example.videoapp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器，处理用户消息通知的相关请求
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取当前用户的所有通知
     * 
     * @param authentication 认证对象
     * @return 通知列表
     */
    @GetMapping
    public Result<List<Notification>> getMyNotifications(Authentication authentication) {
        return Result.success(notificationService.getNotificationsForUser(authentication.getName()));
    }
    
    /**
     * 将指定通知标记为已读
     * 
     * @param notificationId 通知ID
     * @param authentication 认证对象
     * @return 操作结果
     */
    @PostMapping("/{notificationId}/read")
    public Result<Void> markAsRead(@PathVariable Long notificationId, Authentication authentication) {
        notificationService.markNotificationAsRead(notificationId, authentication.getName());
        return Result.success();
    }
    
    /**
     * 将当前用户的所有通知标记为已读
     * 
     * @param authentication 认证对象
     * @return 操作结果
     */
    @PostMapping("/read-all")
    public Result<Void> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsReadForUser(authentication.getName());
        return Result.success();
    }
}
