package com.example.videoapp.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoDetailDTO {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UploaderDTO uploader;
    // 【新增】
    private Integer fishCount;
    @JsonProperty("isFishFed")
    private boolean isFishFed; // 当前用户是否已喂过鱼
}