package com.example.videoapp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 视频实体类
 */
@Data
public class Video {
    /**
     * 视频ID
     */
    private Long id;
    
    /**
     * 上传者用户ID
     */
    private Long userId;
    
    /**
     * 视频标题
     */
    private String title;
    
    /**
     * 视频描述
     */
    private String description;
    
    /**
     * 视频文件URL
     */
    private String videoUrl;
    
    /**
     * 视频封面URL
     */
    private String coverUrl;
    
    /**
     * 播放次数
     */
    private Long viewCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 获得的鱼币数量
     */
    private Integer fishCount;
}
