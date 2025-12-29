package com.azathoth.website.module.forum

import java.time.Instant

/**
 * 用户论坛统计
 */
interface UserForumStats {
    val userId: String
    val postCount: Int
    val replyCount: Int
    val likeReceived: Int
    val level: Int
    val experience: Long
    val badges: List<Badge>
}

/**
 * 徽章
 */
interface Badge {
    val badgeId: String
    val name: String
    val description: String
    val icon: String
    val earnedAt: Instant
}

/**
 * 通知
 */
interface Notification {
    val notificationId: String
    val userId: String
    val type: NotificationType
    val title: String
    val content: String
    val link: String?
    val read: Boolean
    val createdAt: Instant
}
