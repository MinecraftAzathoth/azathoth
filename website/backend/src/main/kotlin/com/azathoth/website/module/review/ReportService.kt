package com.azathoth.website.module.review

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
