package com.azathoth.website.module.forum

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
 * 回复列表结果
 */
interface ReplyListResult {
    val replies: List<ForumReply>
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
