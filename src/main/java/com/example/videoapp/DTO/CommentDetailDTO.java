package com.example.videoapp.DTO;

import lombok.Data;
import java.time.LocalDateTime;

// 接口最终返回的对象
@Data
public class CommentDetailDTO {
    // 包含所有原 Comment 对象的字段
    private Long id;
    private Long videoId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 嵌套一个 CommenterDTO 对象
    private CommenterDTO commenter;
}