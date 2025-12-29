package com.azathoth.website.module.review

import java.time.Instant

/**
 * 审核类型
 */
enum class ReviewType {
    RESOURCE,      // 资源审核
    VERSION,       // 版本审核
    REPORT,        // 举报审核
    WITHDRAW       // 提现审核
}

/**
 * 审核状态
 */
enum class ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NEEDS_REVISION
}

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
 * 举报原因
 */
enum class ReportReason {
    SPAM,
    MALWARE,
    COPYRIGHT,
    INAPPROPRIATE,
    SCAM,
    OTHER
}

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

/**
 * 自动检查服务接口
 */
interface AutoCheckService {
    /**
     * 执行自动检查
     */
    suspend fun runChecks(resourceId: String, fileData: ByteArray): List<ChecklistResult>

    /**
     * 检查恶意代码
     */
    suspend fun scanForMalware(fileData: ByteArray): ScanResult

    /**
     * 检查 API 兼容性
     */
    suspend fun checkApiCompatibility(fileData: ByteArray, targetApiVersion: String): CompatibilityResult

    /**
     * 检查依赖冲突
     */
    suspend fun checkDependencies(fileData: ByteArray): DependencyCheckResult
}

/**
 * 扫描结果
 */
interface ScanResult {
    val clean: Boolean
    val threats: List<Threat>
}

/**
 * 威胁
 */
interface Threat {
    val type: String
    val severity: ThreatSeverity
    val description: String
    val location: String?
}

/**
 * 威胁严重程度
 */
enum class ThreatSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 兼容性结果
 */
interface CompatibilityResult {
    val compatible: Boolean
    val issues: List<CompatibilityIssue>
}

/**
 * 兼容性问题
 */
interface CompatibilityIssue {
    val type: String
    val description: String
    val suggestion: String?
}

/**
 * 依赖检查结果
 */
interface DependencyCheckResult {
    val valid: Boolean
    val missingDependencies: List<String>
    val conflictingDependencies: List<DependencyConflict>
}

/**
 * 依赖冲突
 */
interface DependencyConflict {
    val dependency: String
    val requiredVersion: String
    val conflictingVersion: String
}

/**
 * 举报服务接口
 */
interface ReportService {
    /**
     * 提交举报
     */
    suspend fun submitReport(
        reporterId: String,
        targetType: String,
        targetId: String,
        reason: ReportReason,
        content: String,
        evidence: List<String>
    ): String?

    /**
     * 获取用户举报历史
     */
    suspend fun getUserReports(userId: String): List<ReportDetail>

    /**
     * 处理举报
     */
    suspend fun processReport(
        reportId: String,
        reviewerId: String,
        action: ReportAction,
        notes: String?
    ): Boolean
}

/**
 * 举报处理动作
 */
enum class ReportAction {
    DISMISS,           // 驳回
    WARNING,           // 警告
    REMOVE_CONTENT,    // 删除内容
    SUSPEND_USER,      // 暂停用户
    BAN_USER           // 封禁用户
}
