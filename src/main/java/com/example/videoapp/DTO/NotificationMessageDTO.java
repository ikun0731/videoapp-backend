package com.example.videoapp.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知消息DTO，用于消息队列中传输通知信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessageDTO {
    /**
     * 通知类型，如NEW_COMMENT、NEW_FISH等
     */
    private String type;
    
    /**
     * 发送者用户ID
     */
    private Long senderId;
    
    /**
     * 接收者用户ID
     */
    private Long recipientId;
    
    /**
     * 相关视频ID
     */
    private Long videoId;

    /**
     * 发送者用户名
     */
    private String senderName;
    
    /**
     * 相关视频标题
     */
    private String videoTitle;
}