package com.example.videoapp.DTO;

import lombok.Data;

@Data
public class CommenterDTO {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private String username; // 【核心】新增 username 字段
}