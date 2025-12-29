package com.azathoth.website.module.forum

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
