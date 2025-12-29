package com.azathoth.website.module.review

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
 * 举报处理动作
 */
enum class ReportAction {
    DISMISS,           // 驳回
    WARNING,           // 警告
    REMOVE_CONTENT,    // 删除内容
    SUSPEND_USER,      // 暂停用户
    BAN_USER           // 封禁用户
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
