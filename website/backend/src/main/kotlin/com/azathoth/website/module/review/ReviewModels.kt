package com.azathoth.website.module.review

import java.time.Instant

/**
 * 审核项
 */
interface ReviewItem {
    val reviewId: String
    val type: ReviewType
    val targetId: String
    val targetName: String
    val submitterId: String
    val submitterName: String
    val status: ReviewStatus
    val priority: Int
    val submittedAt: Instant
    val reviewedAt: Instant?
    val reviewerId: String?
    val reviewerName: String?
    val reviewNotes: String?
}

/**
 * 资源审核详情
 */
interface ResourceReviewDetail : ReviewItem {
    val resourceName: String
    val resourceType: String
    val resourceDescription: String
    val downloadUrl: String
    val checklistResults: List<ChecklistResult>
}

/**
 * 检查项结果
 */
interface ChecklistResult {
    val checkId: String
    val name: String
    val description: String
    val passed: Boolean
    val notes: String?
}

/**
 * 举报详情
 */
interface ReportDetail : ReviewItem {
    val reportReason: ReportReason
    val reportContent: String
    val reportedItemType: String
    val reportedItemId: String
    val reportedUserId: String
    val evidence: List<String>
}

/**
 * 审核列表结果
 */
interface ReviewListResult {
    val items: List<ReviewItem>
    val totalCount: Long
    val page: Int
    val totalPages: Int
}

/**
 * 审核员统计
 */
interface ReviewerStats {
    val reviewerId: String
    val totalReviewed: Int
    val approvedCount: Int
    val rejectedCount: Int
    val averageReviewTime: Long  // 毫秒
    val thisMonthCount: Int
}
