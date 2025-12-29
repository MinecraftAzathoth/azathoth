package com.azathoth.website.module.forum

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
