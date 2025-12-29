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
 * 论坛服务接口
 */
interface ForumService {
    /**
     * 获取板块列表
     */
    suspend fun getCategories(): List<ForumCategory>

    /**
     * 获取帖子列表
     */
    suspend fun getPosts(
        categoryId: String?,
        sortBy: PostSortBy,
        page: Int,
        pageSize: Int
    ): PostListResult

    /**
     * 搜索帖子
     */
    suspend fun searchPosts(
        keyword: String,
        categoryId: String?,
        page: Int,
        pageSize: Int
    ): PostListResult

    /**
     * 获取帖子详情
     */
    suspend fun getPost(postId: String): ForumPost?

    /**
     * 创建帖子
     */
    suspend fun createPost(authorId: String, post: CreatePostRequest): ForumPost?

    /**
     * 更新帖子
     */
    suspend fun updatePost(postId: String, update: UpdatePostRequest): ForumPost?

    /**
     * 删除帖子
     */
    suspend fun deletePost(postId: String): Boolean

    /**
     * 置顶帖子
     */
    suspend fun pinPost(postId: String, pinned: Boolean): Boolean

    /**
     * 设为精华
     */
    suspend fun featurePost(postId: String, featured: Boolean): Boolean

    /**
     * 锁定帖子
     */
    suspend fun lockPost(postId: String, locked: Boolean): Boolean
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
 * 帖子列表结果
 */
interface PostListResult {
    val posts: List<ForumPost>
    val totalCount: Long
    val page: Int
    val totalPages: Int
}

/**
 * 创建帖子请求
 */
interface CreatePostRequest {
    val title: String
    val content: String
    val categoryId: String
    val tags: List<String>
}

/**
 * 更新帖子请求
 */
interface UpdatePostRequest {
    val title: String?
    val content: String?
    val tags: List<String>?
}

/**
 * 回复服务接口
 */
interface ReplyService {
    /**
     * 获取帖子回复
     */
    suspend fun getReplies(postId: String, page: Int, pageSize: Int): ReplyListResult

    /**
     * 创建回复
     */
    suspend fun createReply(postId: String, authorId: String, content: String, parentId: String?): ForumReply?

    /**
     * 更新回复
     */
    suspend fun updateReply(replyId: String, content: String): ForumReply?

    /**
     * 删除回复
     */
    suspend fun deleteReply(replyId: String): Boolean
}

/**
 * 回复列表结果
 */
interface ReplyListResult {
    val replies: List<ForumReply>
    val totalCount: Long
    val page: Int
    val totalPages: Int
}

/**
 * 互动服务接口
 */
interface InteractionService {
    /**
     * 点赞帖子
     */
    suspend fun likePost(userId: String, postId: String): Boolean

    /**
     * 取消点赞帖子
     */
    suspend fun unlikePost(userId: String, postId: String): Boolean

    /**
     * 点赞回复
     */
    suspend fun likeReply(userId: String, replyId: String): Boolean

    /**
     * 取消点赞回复
     */
    suspend fun unlikeReply(userId: String, replyId: String): Boolean

    /**
     * 收藏帖子
     */
    suspend fun favoritePost(userId: String, postId: String): Boolean

    /**
     * 取消收藏帖子
     */
    suspend fun unfavoritePost(userId: String, postId: String): Boolean

    /**
     * 获取用户收藏
     */
    suspend fun getUserFavorites(userId: String, page: Int, pageSize: Int): PostListResult

    /**
     * 关注用户
     */
    suspend fun followUser(followerId: String, followeeId: String): Boolean

    /**
     * 取消关注
     */
    suspend fun unfollowUser(followerId: String, followeeId: String): Boolean

    /**
     * 获取关注列表
     */
    suspend fun getFollowing(userId: String): List<String>

    /**
     * 获取粉丝列表
     */
    suspend fun getFollowers(userId: String): List<String>
}

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
