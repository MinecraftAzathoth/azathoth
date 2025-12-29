package com.azathoth.website.module.forum

/**
 * 帖子状态
 */
enum class PostStatus {
    NORMAL,
    PINNED,
    FEATURED,
    LOCKED,
    HIDDEN,
    DELETED
}

/**
 * 帖子排序
 */
enum class PostSortBy {
    LATEST,
    HOT,
    FEATURED,
    REPLIES
}

/**
 * 通知类型
 */
enum class NotificationType {
    SYSTEM,
    REPLY,
    LIKE,
    FOLLOW,
    MENTION,
    PURCHASE,
    EARNING
}
