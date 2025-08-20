package com.example.videoapp.service;

import com.example.videoapp.DTO.VideoDetailDTO;
import com.example.videoapp.entity.User;
import com.example.videoapp.entity.Video;
import com.example.videoapp.mapper.CommentMapper;
import com.example.videoapp.mapper.FishTransactionMapper;
import com.example.videoapp.mapper.UserMapper;
import com.example.videoapp.mapper.VideoMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.videoapp.config.RabbitMQConfig;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class VideoService {

    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TencentCosService tencentCosService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private FishTransactionMapper fishTransactionMapper;

    /**
     * 获取视频详情并更新播放量
     * 
     * @param videoId 视频ID
     * @param currentUsername 当前用户名
     * @return 视频详情DTO
     */
    @CachePut(value = "video:detail", key = "#videoId", unless = "#result == null")
    public VideoDetailDTO findVideoDetailById(Long videoId, String currentUsername) {
        // 更新数据库的播放量
        videoMapper.incrementViewCount(videoId);

        // 获取当前用户ID
        Long currentUserId = null;
        if (currentUsername != null) {
            User currentUser = userMapper.findByUsername(currentUsername);
            if (currentUser != null) {
                currentUserId = currentUser.getId();
            }
        }

        // 返回最新数据，@CachePut会自动用这份新数据覆盖Redis中的旧数据
        return videoMapper.findByIdWithUploader(videoId, currentUserId);
    }

    /**
     * 上传视频
     * 
     * @param file 视频文件
     * @param coverFile 封面文件
     * @param title 视频标题
     * @param description 视频描述
     * @param username 上传用户名
     * @return 创建的视频对象
     */
    public Video uploadVideo(MultipartFile file, MultipartFile coverFile, String title, String description, String username) {
        String videoUrl = tencentCosService.uploadFile(file, "videos/");
        User user = userMapper.findByUsername(username);
        if (user == null) { 
            throw new RuntimeException("找不到当前用户"); 
        }
        
        Video video = new Video();
        video.setUserId(user.getId());
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(videoUrl);
        video.setViewCount(0L);
        video.setCreatedAt(LocalDateTime.now());
        video.setUpdatedAt(LocalDateTime.now());
        video.setFishCount(0);
        
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverUrl = tencentCosService.uploadFile(coverFile, "covers/");
            video.setCoverUrl(coverUrl);
        }
        
        videoMapper.insert(video);
        
        // 发送消息到RabbitMQ进行异步处理
        Long newVideoId = video.getId();
        if (newVideoId != null) {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.VIDEO_EXCHANGE_NAME,
                    RabbitMQConfig.VIDEO_ROUTING_KEY,
                    newVideoId
            );
        }
        
        return video;
    }

    /**
     * 删除视频
     * 
     * @param videoId 视频ID
     * @param username 操作用户名
     */
    @CacheEvict(value = "video:detail", key = "#videoId")
    @Transactional
    public void deleteVideo(Long videoId, String username) {
        Video video = videoMapper.findById(videoId);
        if (video == null) { 
            throw new RuntimeException("视频不存在，无法删除！"); 
        }
        
        User user = userMapper.findByUsername(username);
        if (user == null || !Objects.equals(video.getUserId(), user.getId())) { 
            throw new RuntimeException("你无权删除该视频"); 
        }

        String videoUrl = video.getVideoUrl();
        String coverUrl = video.getCoverUrl();

        // 删除相关数据
        commentMapper.deleteByVideoId(videoId);
        fishTransactionMapper.deleteByVideoId(videoId);
        videoMapper.deleteById(videoId);

        // 删除存储的文件
        if (videoUrl != null && !videoUrl.isEmpty()) {
            tencentCosService.deleteFile(videoUrl);
        }
        
        if (coverUrl != null && !coverUrl.isEmpty()) {
            tencentCosService.deleteFile(coverUrl);
        }
    }

    /**
     * 更新视频信息
     * 
     * @param videoId 视频ID
     * @param title 标题
     * @param description 描述
     * @param coverFile 新的封面文件
     * @param username 操作用户名
     * @return 更新后的视频对象
     */
    @CacheEvict(value = "video:detail", key = "#videoId")
    @Transactional
    public Video updateVideo(Long videoId, String title, String description, MultipartFile coverFile, String username) {
        User currentUser = userMapper.findByUsername(username);
        if (currentUser == null) { 
            throw new RuntimeException("用户不存在"); 
        }
        
        Video videoToUpdate = videoMapper.findById(videoId);
        if (videoToUpdate == null) { 
            throw new RuntimeException("视频不存在"); 
        }
        
        if (!Objects.equals(videoToUpdate.getUserId(), currentUser.getId())) {
            throw new RuntimeException("无权修改该视频");
        }
        
        boolean hasChanges = false;
        
        if (title != null && !title.isBlank()) {
            videoToUpdate.setTitle(title);
            hasChanges = true;
        }
        
        if (description != null) {
            videoToUpdate.setDescription(description);
            hasChanges = true;
        }
        
        if (coverFile != null && !coverFile.isEmpty()) {
            String oldCoverUrl = videoToUpdate.getCoverUrl();
            String newCoverUrl = tencentCosService.uploadFile(coverFile, "covers/");
            videoToUpdate.setCoverUrl(newCoverUrl);
            
            if (oldCoverUrl != null && !oldCoverUrl.isEmpty()) {
                tencentCosService.deleteFile(oldCoverUrl);
            }
            
            hasChanges = true;
        }
        
        if (hasChanges) {
            videoToUpdate.setUpdatedAt(LocalDateTime.now());
            videoMapper.update(videoToUpdate);
        }
        
        return videoToUpdate;
    }

    /**
     * 分页获取所有视频列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序方式
     * @return 分页视频列表
     */
    public PageInfo<Video> findAllVideosPaginated(int page, int size, String sortBy) {
        PageHelper.startPage(page, size);
        List<Video> videos = videoMapper.findAll(sortBy);
        return new PageInfo<>(videos);
    }

    /**
     * 根据关键词搜索视频
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序方式
     * @return 分页视频列表
     */
    public PageInfo<Video> searchVideos(String keyword, int page, int size, String sortBy) {
        PageHelper.startPage(page, size);
        List<Video> videos = videoMapper.searchByTitle(keyword, sortBy);
        return new PageInfo<>(videos);
    }

    /**
     * 获取指定用户的视频列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页视频列表
     */
    public PageInfo<Video> findVideosByUserIdPaginated(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Video> videos = videoMapper.findByUserId(userId);
        return new PageInfo<>(videos);
    }
}