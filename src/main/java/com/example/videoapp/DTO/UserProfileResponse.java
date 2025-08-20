package com.example.videoapp.DTO;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户资料响应DTO
 */
@Data
public class UserProfileResponse {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 账号创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 账号最近更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 鱼币余额
     */
    private Integer fishBalance;
    
    /**
     * 是否可以领取每日奖励
     */
    private Boolean canClaimDaily;
}
