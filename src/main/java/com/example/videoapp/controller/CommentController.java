package com.example.videoapp.controller;

import com.example.videoapp.DTO.CommentDetailDTO;
import com.example.videoapp.DTO.CreateCommentRequest;
import com.example.videoapp.common.Result;
import com.example.videoapp.entity.Comment;
import com.example.videoapp.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器，处理与视频评论相关的请求
 */
@RestController
@RequestMapping("/api")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    /**
     * 发表评论
     * 
     * @param videoId 视频ID
     * @param commentRequest 评论请求对象
     * @param authentication 认证对象
     * @return 创建的评论
     */
    @PostMapping("/videos/{videoId}/comments")
    public Result<Comment> postComment(
            @PathVariable Long videoId,
            @RequestBody CreateCommentRequest commentRequest,
            Authentication authentication) {
        String username = authentication.getName();
        return Result.success(commentService.createComment(videoId, commentRequest.getContent(), username));
    }
    
    /**
     * 获取视频的所有评论
     * 
     * @param videoId 视频ID
     * @return 评论列表
     */
    @GetMapping("/videos/{videoId}/comments")
    public Result<List<CommentDetailDTO>> getCommentsForVideo(@PathVariable Long videoId) {
        return Result.success(commentService.getCommentsByVideoId(videoId));
    }
    
    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @param authentication 认证对象
     * @return 操作结果
     */
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        String username = authentication.getName();
        commentService.deleteComment(commentId, username);
        return Result.success(); 
    }
}
