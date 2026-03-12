package com.azathoth.services.admin.moderation

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

// region Data Classes

data class SimplePunishment(
    override val punishmentId: String,
    override val playerId: PlayerId,
    override val type: PunishmentType,
    override val reason: String,
    override val executorId: PlayerId?,
    override val executedAt: Long = System.currentTimeMillis(),
    override val expiresAt: Long? = null,
    override val revoked: Boolean = false,
    override val revokedBy: PlayerId? = null,
    override val revokedAt: Long? = null,
    override val revokeReason: String? = null
) : Punishment {
    override val isExpired: Boolean
        get() = expiresAt != null && System.currentTimeMillis() > expiresAt
}

data class SimpleReport(
    override val reportId: String,
    override val reporterId: PlayerId,
    override val targetId: PlayerId,
    override val category: ReportCategory,
    override val content: String,
    override val evidence: List<String> = emptyList(),
    override val reportedAt: Long = System.currentTimeMillis(),
    override val status: ReportStatus = ReportStatus.PENDING,
    override val handlerId: PlayerId? = null,
    override val handledAt: Long? = null,
    override val resolution: String? = null
) : Report

// endregion

class DefaultModerationService : ModerationService {

    private val punishments = ConcurrentHashMap<String, SimplePunishment>()
    private val reports = ConcurrentHashMap<String, SimpleReport>()
    private val ipBans = ConcurrentHashMap<String, SimplePunishment>() // ip -> punishment

    override suspend fun warn(playerId: PlayerId, reason: String, executorId: PlayerId): Result<Punishment> {
        val punishment = SimplePunishment(
            punishmentId = UUID.randomUUID().toString(),
            playerId = playerId,
            type = PunishmentType.WARNING,
            reason = reason,
            executorId = executorId
        )
        punishments[punishment.punishmentId] = punishment
        logger.info { "警告玩家 $playerId: $reason (by $executorId)" }
        return Result.success(punishment)
    }

    override suspend fun mute(playerId: PlayerId, duration: Duration, reason: String, executorId: PlayerId): Result<Punishment> {
        if (duration.isNegative()) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "禁言时长无效")

        val punishment = SimplePunishment(
            punishmentId = UUID.randomUUID().toString(),
            playerId = playerId,
            type = PunishmentType.MUTE,
            reason = reason,
            executorId = executorId,
            expiresAt = System.currentTimeMillis() + duration.inWholeMilliseconds
        )
        punishments[punishment.punishmentId] = punishment
        logger.info { "禁言玩家 $playerId ${duration}: $reason" }
        return Result.success(punishment)
    }

    override suspend fun kick(playerId: PlayerId, reason: String, executorId: PlayerId): Result<Punishment> {
        val punishment = SimplePunishment(
            punishmentId = UUID.randomUUID().toString(),
            playerId = playerId,
            type = PunishmentType.KICK,
            reason = reason,
            executorId = executorId
        )
        punishments[punishment.punishmentId] = punishment
        logger.info { "踢出玩家 $playerId: $reason" }
        return Result.success(punishment)
    }

    override suspend fun ban(playerId: PlayerId, duration: Duration?, reason: String, executorId: PlayerId): Result<Punishment> {
        val type = if (duration == null) PunishmentType.PERM_BAN else PunishmentType.TEMP_BAN
        val punishment = SimplePunishment(
            punishmentId = UUID.randomUUID().toString(),
            playerId = playerId,
            type = type,
            reason = reason,
            executorId = executorId,
            expiresAt = duration?.let { System.currentTimeMillis() + it.inWholeMilliseconds }
        )
        punishments[punishment.punishmentId] = punishment
        logger.info { "封禁玩家 $playerId (${duration ?: "永久"}): $reason" }
        return Result.success(punishment)
    }

    override suspend fun banIp(ip: String, duration: Duration?, reason: String, executorId: PlayerId): Result<Punishment> {
        if (ip.isBlank()) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "IP地址不能为空")

        val punishment = SimplePunishment(
            punishmentId = UUID.randomUUID().toString(),
            playerId = PlayerId(ip), // 用IP作为标识
            type = PunishmentType.IP_BAN,
            reason = reason,
            executorId = executorId,
            expiresAt = duration?.let { System.currentTimeMillis() + it.inWholeMilliseconds }
        )
        punishments[punishment.punishmentId] = punishment
        ipBans[ip] = punishment
        logger.info { "封禁IP $ip (${duration ?: "永久"}): $reason" }
        return Result.success(punishment)
    }

    override suspend fun revoke(punishmentId: String, reason: String, executorId: PlayerId): Result<Unit> {
        val punishment = punishments[punishmentId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "处罚记录不存在")
        if (punishment.revoked) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "处罚已被撤销")
        }

        val revoked = punishment.copy(
            revoked = true,
            revokedBy = executorId,
            revokedAt = System.currentTimeMillis(),
            revokeReason = reason
        )
        punishments[punishmentId] = revoked

        // 如果是IP封禁，也从ipBans中移除
        if (punishment.type == PunishmentType.IP_BAN) {
            ipBans.entries.removeIf { it.value.punishmentId == punishmentId }
        }

        logger.info { "撤销处罚 $punishmentId: $reason (by $executorId)" }
        return Result.success(Unit)
    }

    override suspend fun getPunishments(playerId: PlayerId): List<Punishment> {
        return punishments.values
            .filter { it.playerId == playerId }
            .sortedByDescending { it.executedAt }
    }

    override suspend fun getActivePunishment(playerId: PlayerId, type: PunishmentType): Punishment? {
        return punishments.values.find {
            it.playerId == playerId && it.type == type && it.isActive
        }
    }

    override suspend fun isMuted(playerId: PlayerId): Boolean {
        return punishments.values.any {
            it.playerId == playerId && it.type == PunishmentType.MUTE && it.isActive
        }
    }

    override suspend fun isBanned(playerId: PlayerId): Boolean {
        return punishments.values.any {
            it.playerId == playerId &&
                (it.type == PunishmentType.TEMP_BAN || it.type == PunishmentType.PERM_BAN) &&
                it.isActive
        }
    }

    override suspend fun isIpBanned(ip: String): Boolean {
        return ipBans[ip]?.isActive == true
    }

    override suspend fun submitReport(
        reporterId: PlayerId,
        targetId: PlayerId,
        category: ReportCategory,
        content: String,
        evidence: List<String>
    ): Result<Report> {
        if (reporterId == targetId) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "不能举报自己")
        }
        if (content.isBlank()) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "举报内容不能为空")
        }

        val report = SimpleReport(
            reportId = UUID.randomUUID().toString(),
            reporterId = reporterId,
            targetId = targetId,
            category = category,
            content = content,
            evidence = evidence
        )
        reports[report.reportId] = report
        logger.info { "收到举报: $reporterId -> $targetId ($category)" }
        return Result.success(report)
    }

    override suspend fun handleReport(
        reportId: String,
        handlerId: PlayerId,
        status: ReportStatus,
        resolution: String
    ): Result<Unit> {
        val report = reports[reportId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "举报不存在")
        if (report.status == ReportStatus.RESOLVED || report.status == ReportStatus.REJECTED) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "举报已处理")
        }

        reports[reportId] = report.copy(
            status = status,
            handlerId = handlerId,
            handledAt = System.currentTimeMillis(),
            resolution = resolution
        )
        logger.info { "处理举报 $reportId: $status - $resolution" }
        return Result.success(Unit)
    }

    override suspend fun getPendingReports(limit: Int): List<Report> {
        return reports.values
            .filter { it.status == ReportStatus.PENDING }
            .sortedBy { it.reportedAt }
            .take(limit)
    }

    override suspend fun getReport(reportId: String): Report? = reports[reportId]
}
