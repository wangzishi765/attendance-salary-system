package com.example.hrms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.hrms.entity.Notification;
import com.example.hrms.mapper.NotificationMapper;
import com.example.hrms.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    public Page<Notification> myNotifications(int current, int size, Integer isRead) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (isRead != null) {
            wrapper.eq(Notification::getIsRead, isRead);
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(new Page<>(current, size), wrapper);
    }

    public int unreadCount() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        return Math.toIntExact(notificationMapper.selectCount(wrapper));
    }

    public void markAsRead(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && n.getUserId().equals(SecurityUtil.getCurrentUser().getId())) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    public void markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Notification update = new Notification();
        update.setIsRead(1);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0);
        notificationMapper.update(update, wrapper);
    }

    public void sendNotification(Long userId, String title, String content, String type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setIsRead(0);
        n.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(n);
    }

    public Map<String, Object> overview() {
        Long userId = SecurityUtil.getCurrentUser().getId();
        Map<String, Object> result = new HashMap<>();
        result.put("unread", unreadCount());
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)
               .orderByDesc(Notification::getCreateTime)
               .last("LIMIT 5");
        result.put("latest", notificationMapper.selectList(wrapper));
        return result;
    }
}
