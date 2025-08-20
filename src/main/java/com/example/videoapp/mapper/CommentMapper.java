package com.example.videoapp.mapper;

import com.example.videoapp.DTO.CommentDetailDTO;
import com.example.videoapp.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 评论数据访问接口
 */
@Mapper
public interface CommentMapper {
    /**
     * 插入新评论
     *
     * @param comment 评论对象
     * @return 受影响的行数
     */
    int insert(Comment comment);
    
    /**
     * 查找指定视频的所有评论
     *
     * @param videoId 视频ID
     * @return 评论详情DTO列表
     */
    List<CommentDetailDTO> findByVideoId(Long videoId);
    
    /**
     * 根据ID查找评论
     *
     * @param id 评论ID
     * @return 评论对象
     */
    Comment findById(Long id);
    
    /**
     * 根据ID删除评论
     *
     * @param id 评论ID
     * @return 受影响的行数
     */
    int deleteById(Long id);
    
    /**
     * 删除指定视频的所有评论
     *
     * @param videoId 视频ID
     */
    void deleteByVideoId(Long videoId);
}
