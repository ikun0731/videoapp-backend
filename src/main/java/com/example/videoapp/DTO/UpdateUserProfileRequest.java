package com.example.videoapp.DTO;

import lombok.Data;

/**
 * 更新用户资料请求DTO
 */
@Data
public class UpdateUserProfileRequest {
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
}
