package com.example.videoapp.mapper;

import com.example.videoapp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户数据访问接口
 */
@Mapper
public interface UserMapper {

    /**
     * 插入一个新用户
     *
     * @param user 待插入的用户对象
     * @return 受影响的行数
     */
    int insert(User user);
    
    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户对象，不存在则返回null
     */
    User findByUsername(String username);
    
    /**
     * 根据昵称查找用户
     *
     * @param nickname 昵称
     * @return 用户对象，不存在则返回null
     */
    User findByNickname(String nickname);
    
    /**
     * 更新用户资料
     *
     * @param user 包含更新信息的用户对象
     * @return 受影响的行数
     */
    int updateProfile(User user);
    
    /**
     * 根据邮箱查找用户
     *
     * @param email 邮箱
     * @return 用户对象，不存在则返回null
     */
    User findByEmail(String email);
    
    /**
     * 根据昵称关键字搜索用户
     *
     * @param keyword 搜索关键词
     * @return 匹配的用户列表
     */
    List<User> searchByNickName(String keyword);
    
    /**
     * 更新用户鱼币余额和签到日期
     *
     * @param id 用户ID
     * @param fishBalance 更新后的鱼币余额
     * @param lastDailyClaim 最近签到日期
     * @return 受影响的行数
     */
    int updateUserFish(@Param("id") Long id,
                       @Param("fishBalance") Integer fishBalance,
                       @Param("lastDailyClaim") LocalDate lastDailyClaim);
    
    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户对象，不存在则返回null
     */
    User findById(Long id);
}