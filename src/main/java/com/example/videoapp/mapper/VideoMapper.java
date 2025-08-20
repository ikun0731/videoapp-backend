package com.example.videoapp.mapper;

import com.example.videoapp.DTO.VideoDetailDTO;
import com.example.videoapp.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 视频数据访问接口
 */
@Mapper
public interface VideoMapper {
    /**
     * 插入新视频
     *
     * @param video 视频对象
     * @return 受影响的行数
     */
    int insert(Video video);
    
    /**
     * 查找所有视频，并按指定方式排序
     *
     * @param sortBy 排序方式
     * @return 视频列表
     */
    List<Video> findAll(@Param("sortBy") String sortBy);
    
    /**
     * 根据ID查找视频
     *
     * @param id 视频ID
     * @return 视频对象
     */
    Video findById(Long id);
    
    /**
     * 根据ID查找视频，并包含上传者信息
     *
     * @param id 视频ID
     * @return 视频详情DTO
     */
    VideoDetailDTO findByIdWithUploader(Long id);
    
    /**
     * 查找指定用户上传的视频
     *
     * @param userId 用户ID
     * @return 视频列表
     */
    List<Video> findByUserId(Long userId);
    
    /**
     * 根据标题搜索视频，并按指定方式排序
     *
     * @param keyword 搜索关键词
     * @param sortBy 排序方式
     * @return 视频列表
     */
    List<Video> searchByTitle(@Param("keyword") String keyword, @Param("sortBy") String sortBy);
    
    /**
     * 增加视频播放量
     *
     * @param id 视频ID
     */
    void incrementViewCount(Long id);
    
    /**
     * 根据ID删除视频
     *
     * @param id 视频ID
     * @return 受影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 根据ID查找视频，包含上传者信息，并检查当前用户是否已点赞
     *
     * @param videoId 视频ID
     * @param currentUserId 当前用户ID
     * @return 视频详情DTO
     */
    VideoDetailDTO findByIdWithUploader(@Param("videoId") Long videoId, @Param("currentUserId") Long currentUserId);
    
    /**
     * 增加视频获得的鱼币数量
     *
     * @param videoId 视频ID
     * @return 受影响的行数
     */
    int incrementFishCount(Long videoId);
    
    /**
     * 更新视频信息
     *
     * @param video 视频对象
     * @return 受影响的行数
     */
    int update(Video video);
}