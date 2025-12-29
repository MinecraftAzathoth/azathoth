package com.azathoth.website.module.market

/**
 * 评论服务接口
 */
interface ReviewService {
    /**
     * 获取资源评论
     */
    suspend fun getReviews(resourceId: String, page: Int, pageSize: Int): List<ResourceReview>

    /**
     * 创建评论
     */
    suspend fun createReview(resourceId: String, userId: String, rating: Int, content: String): ResourceReview?

    /**
     * 更新评论
     */
    suspend fun updateReview(reviewId: String, rating: Int?, content: String?): ResourceReview?

    /**
     * 删除评论
     */
    suspend fun deleteReview(reviewId: String): Boolean

    /**
     * 作者回复
     */
    suspend fun replyToReview(reviewId: String, reply: String): ResourceReview?

    /**
     * 标记有帮助
     */
    suspend fun markHelpful(reviewId: String, userId: String): Boolean
}

/**
 * 下载服务接口
 */
interface DownloadService {
    /**
     * 获取下载链接
     */
    suspend fun getDownloadUrl(resourceId: String, version: String, userId: String?): String?

    /**
     * 记录下载
     */
    suspend fun recordDownload(resourceId: String, version: String, userId: String?): Boolean

    /**
     * 获取下载统计
     */
    suspend fun getDownloadStats(resourceId: String): DownloadStats
}
