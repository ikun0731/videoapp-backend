package com.example.videoapp.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 鱼币交易实体类，记录用户给视频投喂鱼币的交易
 */
@Data
public class FishTransaction {
    /**
     * 交易ID
     */
    private Long id;
    
    /**
     * 交易用户ID
     */
    private Long userId;
    
    /**
     * 目标视频ID
     */
    private Long videoId;
    
    /**
     * 交易金额（鱼币数量）
     */
    private Integer amount;
    
    /**
     * 交易创建时间
     */
    private LocalDateTime createdAt;
}