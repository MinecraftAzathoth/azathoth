package com.azathoth.website.module.forum

/**
 * 通知服务接口
 */
interface NotificationService {
    /**
     * 获取通知列表
     */
    suspend fun getNotifications(userId: String, page: Int, pageSize: Int): List<Notification>

    /**
     * 获取未读数量
     */
    suspend fun getUnreadCount(userId: String): Int

    /**
     * 标记已读
     */
    suspend fun markAsRead(notificationId: String): Boolean

    /**
     * 标记全部已读
     */
    suspend fun markAllAsRead(userId: String): Boolean
}
