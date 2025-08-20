package com.example.videoapp.controller;

import com.example.videoapp.DTO.UserProfileResponse;
import com.example.videoapp.DTO.VideoDetailDTO;
import com.example.videoapp.common.Result;
import com.example.videoapp.entity.Video;
import com.example.videoapp.service.FishService;
import com.example.videoapp.service.UserService;
import com.example.videoapp.service.VideoService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;
    
    @Autowired
    private FishService fishService;
    
    @Autowired
    private UserService userService;

    /**
     * 上传视频
     * 
     * @param file 视频文件
     * @param coverFile 封面文件
     * @param title 视频标题
     * @param description 视频描述
     * @param authentication 认证对象
     * @return 上传的视频对象
     */
    @PostMapping("/upload")
    public Result<Video> upload(
            @RequestParam("file") MultipartFile file, 
            @RequestParam(value = "cover", required = false) MultipartFile coverFile, 
            @RequestParam String title, 
            @RequestParam String description, 
            Authentication authentication) {
        return Result.success(videoService.uploadVideo(file, coverFile, title, description, authentication.getName()));
    }

    /**
     * 获取所有视频列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序方式
     * @return 分页视频列表
     */
    @GetMapping
    public Result<PageInfo<Video>> getAllVideos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return Result.success(videoService.findAllVideosPaginated(page, size, sortBy));
    }

    /**
     * 获取视频详情
     * 
     * @param videoId 视频ID
     * @param authentication 认证对象
     * @return 视频详情DTO
     */
    @GetMapping("/{videoId}")
    public Result<VideoDetailDTO> getVideoById(@PathVariable Long videoId, Authentication authentication) {
        String currentUsername = null;
        if (authentication != null && authentication.isAuthenticated()) {
            currentUsername = authentication.getName();
        }
        VideoDetailDTO videoDetail = videoService.findVideoDetailById(videoId, currentUsername);
        if (videoDetail == null) {
            return Result.error(404, "视频不存在");
        }
        return Result.success(videoDetail);
    }

    /**
     * 搜索视频
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序方式
     * @return 分页视频列表
     */
    @GetMapping("/search")
    public Result<PageInfo<Video>> searchVideos(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return Result.success(videoService.searchVideos(keyword, page, size, sortBy));
    }

    /**
     * 删除视频
     * 
     * @param videoId 视频ID
     * @param authentication 认证对象
     * @return 操作结果
     */
    @DeleteMapping("/{videoId}")
    public Result<Void> deleteVideo(@PathVariable Long videoId, Authentication authentication) {
        videoService.deleteVideo(videoId, authentication.getName());
        return Result.success();
    }

    /**
     * 给视频点赞（投喂小鱼）
     * 
     * @param videoId 视频ID
     * @param authentication 认证对象
     * @return 操作结果
     */
    @PostMapping("/{videoId}/feed")
    public Result<Void> feedFish(@PathVariable Long videoId, Authentication authentication) {
        String username = authentication.getName();
        UserProfileResponse userInfo = userService.getUserInfoByUsername(username);
        if (userInfo == null) {
            throw new RuntimeException("认证失败，找不到当前用户");
        }
        fishService.feedFishToVideo(userInfo.getId(), videoId);
        return Result.success();
    }

    /**
     * 更新视频信息
     * 
     * @param videoId 视频ID
     * @param title 新标题
     * @param description 新描述
     * @param cover 新封面
     * @param authentication 认证对象
     * @return 更新后的视频对象
     */
    @PatchMapping("/{videoId}")
    public Result<Video> updateVideo(
            @PathVariable Long videoId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile cover,
            Authentication authentication) {
        String username = authentication.getName();
        Video updatedVideo = videoService.updateVideo(videoId, title, description, cover, username);
        return Result.success(updatedVideo);
    }
}