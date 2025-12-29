package com.azathoth.services.admin.moderation

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import kotlin.time.Duration

/**
 * 处罚类型
 */
enum class PunishmentType {
    /** 警告 */
    WARNING,
    /** 禁言 */
    MUTE,
    /** 踢出 */
    KICK,
    /** 临时封禁 */
    TEMP_BAN,
    /** 永久封禁 */
    PERM_BAN,
    /** IP封禁 */
    IP_BAN
}

/**
 * 处罚记录
 */
interface Punishment {
    /** 处罚ID */
    val punishmentId: String
    
    /** 被处罚玩家ID */
    val playerId: PlayerId
    
    /** 处罚类型 */
    val type: PunishmentType
    
    /** 原因 */
    val reason: String
    
    /** 执行者ID */
    val executorId: PlayerId?
    
    /** 执行时间 */
    val executedAt: Long
    
    /** 过期时间 */
    val expiresAt: Long?
    
    /** 是否已撤销 */
    val revoked: Boolean
    
    /** 撤销者ID */
    val revokedBy: PlayerId?
    
    /** 撤销时间 */
    val revokedAt: Long?
    
    /** 撤销原因 */
    val revokeReason: String?
    
    /** 是否已过期 */
    val isExpired: Boolean
    
    /** 是否生效中 */
    val isActive: Boolean get() = !revoked && !isExpired
}

/**
 * 举报信息
 */
interface Report {
    /** 举报ID */
    val reportId: String
    
    /** 举报者ID */
    val reporterId: PlayerId
    
    /** 被举报者ID */
    val targetId: PlayerId
    
    /** 举报类型 */
    val category: ReportCategory
    
    /** 举报内容 */
    val content: String
    
    /** 证据（截图URL等） */
    val evidence: List<String>
    
    /** 举报时间 */
    val reportedAt: Long
    
    /** 处理状态 */
    val status: ReportStatus
    
    /** 处理者ID */
    val handlerId: PlayerId?
    
    /** 处理时间 */
    val handledAt: Long?
    
    /** 处理结果 */
    val resolution: String?
}

/**
 * 举报类型
 */
enum class ReportCategory {
    /** 作弊 */
    CHEATING,
    /** 辱骂 */
    HARASSMENT,
    /** 不当言论 */
    INAPPROPRIATE_CONTENT,
    /** 诈骗 */
    SCAM,
    /** BUG利用 */
    BUG_EXPLOIT,
    /** 其他 */
    OTHER
}

/**
 * 举报状态
 */
enum class ReportStatus {
    /** 待处理 */
    PENDING,
    /** 处理中 */
    IN_PROGRESS,
    /** 已处理 */
    RESOLVED,
    /** 已驳回 */
    REJECTED
}

/**
 * 管理服务
 */
interface ModerationService {
    /**
     * 警告玩家
     */
    suspend fun warn(
        playerId: PlayerId,
        reason: String,
        executorId: PlayerId
    ): Result<Punishment>
    
    /**
     * 禁言玩家
     */
    suspend fun mute(
        playerId: PlayerId,
        duration: Duration,
        reason: String,
        executorId: PlayerId
    ): Result<Punishment>
    
    /**
     * 踢出玩家
     */
    suspend fun kick(
        playerId: PlayerId,
        reason: String,
        executorId: PlayerId
    ): Result<Punishment>
    
    /**
     * 封禁玩家
     */
    suspend fun ban(
        playerId: PlayerId,
        duration: Duration?,
        reason: String,
        executorId: PlayerId
    ): Result<Punishment>
    
    /**
     * 封禁IP
     */
    suspend fun banIp(
        ip: String,
        duration: Duration?,
        reason: String,
        executorId: PlayerId
    ): Result<Punishment>
    
    /**
     * 撤销处罚
     */
    suspend fun revoke(
        punishmentId: String,
        reason: String,
        executorId: PlayerId
    ): Result<Unit>
    
    /**
     * 获取玩家的处罚记录
     */
    suspend fun getPunishments(playerId: PlayerId): List<Punishment>
    
    /**
     * 获取活跃处罚
     */
    suspend fun getActivePunishment(
        playerId: PlayerId,
        type: PunishmentType
    ): Punishment?
    
    /**
     * 检查是否被禁言
     */
    suspend fun isMuted(playerId: PlayerId): Boolean
    
    /**
     * 检查是否被封禁
     */
    suspend fun isBanned(playerId: PlayerId): Boolean
    
    /**
     * 检查IP是否被封禁
     */
    suspend fun isIpBanned(ip: String): Boolean
    
    /**
     * 提交举报
     */
    suspend fun submitReport(
        reporterId: PlayerId,
        targetId: PlayerId,
        category: ReportCategory,
        content: String,
        evidence: List<String> = emptyList()
    ): Result<Report>
    
    /**
     * 处理举报
     */
    suspend fun handleReport(
        reportId: String,
        handlerId: PlayerId,
        status: ReportStatus,
        resolution: String
    ): Result<Unit>
    
    /**
     * 获取待处理举报
     */
    suspend fun getPendingReports(limit: Int = 50): List<Report>
    
    /**
     * 获取举报详情
     */
    suspend fun getReport(reportId: String): Report?
}
