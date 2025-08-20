package com.example.videoapp.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名（登录账号）
     */
    private String username;
    
    /**
     * 昵称（显示名）
     */
    private String nickname;
    
    /**
     * 密码（加密存储）
     */
    private String password;
    
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 鱼币余额
     */
    private Integer fishBalance;
    
    /**
     * 最近一次领取每日奖励的日期
     */
    private LocalDate lastDailyClaim;
}
