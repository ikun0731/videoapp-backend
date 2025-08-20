package com.example.videoapp.mapper;

import com.example.videoapp.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NotificationMapper {
    void insert(Notification notification);
    /**
     * 根据接收者ID，按时间倒序查询通知列表
     * @param recipientId 接收者用户ID
     * @return 通知列表
     */
    List<Notification> findByRecipientId(Long recipientId);

    /**
     * 根据通知ID查询通知
     * @param id 通知ID
     * @return 通知对象
     */
    Notification findById(Long id);

    /**
     * 更新通知（主要用于标记已读）
     * @param notification 要更新的通知对象
     */
    void update(Notification notification);
    void markAllAsReadByRecipientId(Long recipientId); // 新增
}
