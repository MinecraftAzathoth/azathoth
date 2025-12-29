package com.azathoth.website.module.review

/**
 * 审核服务接口
 */
interface ReviewService {
    /**
     * 获取待审核列表
     */
    suspend fun getPendingReviews(
        type: ReviewType?,
        page: Int,
        pageSize: Int
    ): ReviewListResult

    /**
     * 获取审核详情
     */
    suspend fun getReviewDetail(reviewId: String): ReviewItem?

    /**
     * 领取审核任务
     */
    suspend fun claimReview(reviewId: String, reviewerId: String): Boolean

    /**
     * 放弃审核任务
     */
    suspend fun unclaimReview(reviewId: String): Boolean

    /**
     * 通过审核
     */
    suspend fun approve(reviewId: String, reviewerId: String, notes: String?): Boolean

    /**
     * 拒绝审核
     */
    suspend fun reject(reviewId: String, reviewerId: String, reason: String): Boolean

    /**
     * 要求修改
     */
    suspend fun requestRevision(reviewId: String, reviewerId: String, feedback: String): Boolean

    /**
     * 获取审核历史
     */
    suspend fun getReviewHistory(targetId: String): List<ReviewItem>

    /**
     * 获取审核员统计
     */
    suspend fun getReviewerStats(reviewerId: String): ReviewerStats
}
