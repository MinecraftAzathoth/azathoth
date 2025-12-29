package com.azathoth.website.module.forum

import java.time.Instant

/**
 * 板块分类
 */
interface ForumCategory {
    val categoryId: String
    val name: String
    val description: String
    val icon: String
    val order: Int
    val postCount: Long
    val lastPostAt: Instant?
}

/**
 * 帖子
 */
interface ForumPost {
    val postId: String
    val title: String
    val content: String
    val authorId: String
    val authorName: String
    val authorAvatar: String?
    val categoryId: String
    val tags: List<String>
    val status: PostStatus
    val isPinned: Boolean
    val isFeatured: Boolean
    val isLocked: Boolean
    val viewCount: Long
    val likeCount: Int
    val replyCount: Int
    val createdAt: Instant
    val updatedAt: Instant
    val lastReplyAt: Instant?
}

/**
 * 回复
 */
interface ForumReply {
    val replyId: String
    val postId: String
    val parentId: String?     // 楼中楼
    val content: String
    val authorId: String
    val authorName: String
    val authorAvatar: String?
    val likeCount: Int
    val floor: Int
    val createdAt: Instant
    val updatedAt: Instant?
}
