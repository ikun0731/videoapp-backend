package com.example.videoapp.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
public class Notification {
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 通知接收者用户ID
     */
    private Long recipientId;
    
    /**
     * 通知发送者用户ID
     */
    private Long senderId;
    
    /**
     * 通知类型，如NEW_COMMENT, NEW_FISH等
     */
    private String type;
    
    /**
     * 相关实体ID，如视频ID
     */
    private Long relatedEntityId;
    
    /**
     * 通知内容文本
     */
    private String content;

    /**
     * 是否已读标记
     */
    @JsonProperty("isRead")
    private boolean isRead;

    /**
     * 通知创建时间
     */
    private LocalDateTime createdAt;
}