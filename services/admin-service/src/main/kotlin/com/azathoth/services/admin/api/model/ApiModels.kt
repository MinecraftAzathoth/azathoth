package com.azathoth.services.admin.api.model

import kotlinx.serialization.Serializable

// ─── 通用 ────────────────────────────────────────────────

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null
)

@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

// ─── 认证 ────────────────────────────────────────────────

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val remember: Boolean = false
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String,
    val tokenType: String = "Bearer"
)

@Serializable
data class UserInfo(
    val userId: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val role: String,
    val permissions: List<String>,
    val verified: Boolean,
    val createdAt: String,
    val lastLoginAt: String? = null
)

@Serializable
data class AuthResult(
    val success: Boolean,
    val user: UserInfo? = null,
    val token: AuthToken? = null,
    val error: String? = null
)

// ─── 玩家 ────────────────────────────────────────────────

@Serializable
data class BanStatusDto(
    val banned: Boolean,
    val reason: String? = null,
    val bannedAt: String? = null,
    val expiresAt: String? = null,
    val bannedBy: String? = null
)

@Serializable
data class PlayerInfoDto(
    val playerId: String,
    val username: String,
    val displayName: String,
    val level: Int,
    val experience: Long,
    val gold: Long,
    val diamond: Long,
    val vipLevel: Int,
    val guildId: String? = null,
    val guildName: String? = null,
    val online: Boolean,
    val currentInstance: String? = null,
    val lastLoginAt: String? = null,
    val createdAt: String,
    val banStatus: BanStatusDto? = null
)

@Serializable
data class PlayerStatsDto(
    val totalPlayers: Int,
    val onlinePlayers: Int,
    val newPlayersToday: Int,
    val newPlayersThisWeek: Int,
    val activePlayersToday: Int
)

// ─── 实例 ────────────────────────────────────────────────

@Serializable
data class InstanceInfoDto(
    val instanceId: String,
    val instanceType: String,
    val templateId: String? = null,
    val templateName: String? = null,
    val state: String,
    val playerCount: Int,
    val maxPlayers: Int,
    val cpu: Int,
    val memory: Int,
    val createdAt: String,
    val region: String
)

@Serializable
data class InstanceStatsDto(
    val totalInstances: Int,
    val activeInstances: Int,
    val cpuUsage: Int,
    val memoryUsage: Int,
    val byType: Map<String, Int>
)

// ─── 活动 ────────────────────────────────────────────────

@Serializable
data class ActivityInfoDto(
    val activityId: String,
    val name: String,
    val description: String,
    val type: String,
    val state: String,
    val startTime: String,
    val endTime: String,
    val participantCount: Int
)

// ─── 公告 ────────────────────────────────────────────────

@Serializable
data class AnnouncementInfoDto(
    val announcementId: String,
    val title: String,
    val content: String,
    val type: String,
    val publishedAt: String? = null,
    val expiresAt: String? = null,
    val published: Boolean,
    val authorId: String,
    val authorName: String
)

@Serializable
data class CreateAnnouncementRequest(
    val title: String,
    val content: String,
    val type: String = "NORMAL"
)

// ─── 日志 ────────────────────────────────────────────────

@Serializable
data class LogEntryDto(
    val timestamp: String,
    val level: String,
    val logger: String,
    val message: String,
    val instanceId: String? = null,
    val playerId: String? = null,
    val stackTrace: String? = null
)

// ─── 分析 ────────────────────────────────────────────────

@Serializable
data class AnalyticsDataDto(
    val timestamp: String,
    val onlinePlayers: Int,
    val activeInstances: Int,
    val transactions: Int,
    val revenue: Int
)
