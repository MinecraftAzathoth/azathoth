package com.azathoth.website.module.market.repository

import com.azathoth.website.module.market.dto.*
import com.azathoth.website.module.market.table.*
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class ReviewRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * 获取资源评论
     */
    suspend fun getReviews(resourceId: UUID, page: Int, pageSize: Int): Pair<List<ResourceReviewDTO>, Long> = dbQuery {
        val totalCount = ResourceReviews.selectAll()
            .where { ResourceReviews.resourceId eq resourceId }
            .count()

        val offset = ((page - 1) * pageSize).toLong()
        val reviews = ResourceReviews.selectAll()
            .where { ResourceReviews.resourceId eq resourceId }
            .orderBy(ResourceReviews.createdAt to SortOrder.DESC)
            .limit(pageSize).offset(offset)
            .map { toReviewDTO(it) }

        Pair(reviews, totalCount)
    }

    /**
     * 创建评论
     */
    suspend fun createReview(
        resourceId: UUID,
        userId: UUID,
        userName: String,
        rating: Int,
        content: String
    ): ResourceReviewDTO? = dbQuery {
        // 检查是否已评论
        val existing = ResourceReviews.selectAll()
            .where { (ResourceReviews.resourceId eq resourceId) and (ResourceReviews.userId eq userId) }
            .singleOrNull()

        if (existing != null) return@dbQuery null

        val now = Clock.System.now().toJavaInstant()
        val reviewId = ResourceReviews.insertAndGetId {
            it[ResourceReviews.resourceId] = resourceId
            it[ResourceReviews.userId] = userId
            it[ResourceReviews.userName] = userName
            it[ResourceReviews.rating] = rating
            it[ResourceReviews.content] = content
            it[createdAt] = now
        }.value

        // 更新资源评分和评论数
        updateResourceRating(resourceId)

        ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull()
            ?.let { toReviewDTO(it) }
    }

    /**
     * 更新评论
     */
    suspend fun updateReview(reviewId: UUID, rating: Int?, content: String?): ResourceReviewDTO? = dbQuery {
        val now = Clock.System.now().toJavaInstant()

        val review = ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull() ?: return@dbQuery null

        ResourceReviews.update({ ResourceReviews.id eq reviewId }) {
            rating?.let { r -> it[ResourceReviews.rating] = r }
            content?.let { c -> it[ResourceReviews.content] = c }
            it[updatedAt] = now
        }

        // 更新资源评分
        val resourceId = review[ResourceReviews.resourceId]
        updateResourceRating(resourceId)

        ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull()
            ?.let { toReviewDTO(it) }
    }

    /**
     * 删除评论
     */
    suspend fun deleteReview(reviewId: UUID): Boolean = dbQuery {
        val review = ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull() ?: return@dbQuery false

        val resourceId = review[ResourceReviews.resourceId]

        ReviewHelpful.deleteWhere { ReviewHelpful.reviewId eq reviewId }
        val deleted = ResourceReviews.deleteWhere { ResourceReviews.id eq reviewId } > 0

        if (deleted) {
            updateResourceRating(resourceId)
        }

        deleted
    }

    /**
     * 作者回复
     */
    suspend fun replyToReview(reviewId: UUID, reply: String): ResourceReviewDTO? = dbQuery {
        val now = Clock.System.now().toJavaInstant()

        ResourceReviews.update({ ResourceReviews.id eq reviewId }) {
            it[authorReply] = reply
            it[updatedAt] = now
        }

        ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull()
            ?.let { toReviewDTO(it) }
    }

    /**
     * 标记评论有帮助
     */
    suspend fun markHelpful(reviewId: UUID, userId: UUID): Boolean = dbQuery {
        // 检查是否已标记
        val existing = ReviewHelpful.selectAll()
            .where { (ReviewHelpful.reviewId eq reviewId) and (ReviewHelpful.userId eq userId) }
            .singleOrNull()

        if (existing != null) return@dbQuery false

        ReviewHelpful.insert {
            it[ReviewHelpful.reviewId] = reviewId
            it[ReviewHelpful.userId] = userId
        }

        ResourceReviews.update({ ResourceReviews.id eq reviewId }) {
            it[helpful] = helpful + 1
        }

        true
    }

    /**
     * 获取评论所属资源的作者ID
     */
    suspend fun getReviewResourceAuthorId(reviewId: UUID): UUID? = dbQuery {
        val review = ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull() ?: return@dbQuery null

        val resourceId = review[ResourceReviews.resourceId]

        Resources.selectAll()
            .where { Resources.id eq resourceId }
            .singleOrNull()
            ?.get(Resources.authorId)
    }

    /**
     * 获取评论的用户ID
     */
    suspend fun getReviewUserId(reviewId: UUID): UUID? = dbQuery {
        ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull()
            ?.get(ResourceReviews.userId)
    }

    // ========== 私有辅助方法 ==========

    private fun toReviewDTO(row: ResultRow): ResourceReviewDTO =
        ResourceReviewDTO(
            reviewId = row[ResourceReviews.id].value.toString(),
            resourceId = row[ResourceReviews.resourceId].toString(),
            userId = row[ResourceReviews.userId].toString(),
            userName = row[ResourceReviews.userName],
            rating = row[ResourceReviews.rating],
            content = row[ResourceReviews.content],
            createdAt = row[ResourceReviews.createdAt].toString(),
            updatedAt = row[ResourceReviews.updatedAt]?.toString(),
            helpful = row[ResourceReviews.helpful],
            authorReply = row[ResourceReviews.authorReply]
        )

    private fun updateResourceRating(resourceId: UUID) {
        val stats = ResourceReviews.select(
            ResourceReviews.rating.avg(),
            ResourceReviews.rating.count()
        ).where { ResourceReviews.resourceId eq resourceId }
            .single()

        val avgRating = stats[ResourceReviews.rating.avg()]?.toDouble() ?: 0.0
        val count = stats[ResourceReviews.rating.count()].toInt()

        Resources.update({ Resources.id eq resourceId }) {
            it[rating] = avgRating
            it[reviewCount] = count
        }
    }
}
