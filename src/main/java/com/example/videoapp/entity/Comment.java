package com.example.videoapp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
public class Comment {
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 评论所属视频ID
     */
    private Long videoId;
    
    /**
     * 评论发布者用户ID
     */
    private Long userId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 评论更新时间
     */
    private LocalDateTime updatedAt;
}
