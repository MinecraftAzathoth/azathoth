package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import java.time.Duration
import java.time.Instant

/**
 * 副本难度
 */
enum class DungeonDifficulty {
    NORMAL,
    HARD,
    NIGHTMARE,
    HELL
}

/**
 * 副本状态
 */
enum class DungeonInstanceState {
    CREATING,
    WAITING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CLOSED
}

/**
 * 副本评级
 */
enum class DungeonRating {
    F, D, C, B, A, S
}

/**
 * 匹配状态
 */
enum class MatchmakingStatus {
    QUEUED,
    MATCHING,
    FOUND,
    CANCELLED,
    TIMEOUT
}

/**
 * 副本模板信息
 */
interface DungeonTemplateInfo {
    val templateId: String
    val name: String
    val description: String
    val minPlayers: Int
    val maxPlayers: Int
    val recommendedLevel: Int
    val minLevel: Int
    val supportedDifficulties: List<DungeonDifficulty>
    val timeLimit: Duration
    val dailyEntryLimit: Int
    val weeklyEntryLimit: Int
}

/**
 * 副本实例信息
 */
interface DungeonInstanceInfo {
    val instanceId: String
    val templateId: String
    val difficulty: DungeonDifficulty
    val state: DungeonInstanceState
    val playerIds: Set<PlayerId>
    val leaderId: PlayerId
    val createdAt: Instant
    val startedAt: Instant?
    val completedAt: Instant?
    val elapsedTime: Duration
    val remainingTime: Duration?
    val currentPhase: Int
    val totalPhases: Int
}

/**
 * 副本进度
 */
interface DungeonProgress {
    val instanceId: String
    val currentPhase: Int
    val totalPhases: Int
    val monstersKilled: Int
    val bossesKilled: Int
    val deaths: Int
    val score: Int
    val objectives: List<ObjectiveProgress>
}

/**
 * 目标进度
 */
interface ObjectiveProgress {
    val objectiveId: String
    val name: String
    val description: String
    val current: Int
    val target: Int
    val completed: Boolean
}

/**
 * 副本结算结果
 */
interface DungeonResult {
    val instanceId: String
    val templateId: String
    val difficulty: DungeonDifficulty
    val success: Boolean
    val rating: DungeonRating
    val duration: Duration
    val score: Int
    val participants: List<ParticipantResult>
    val completedAt: Instant
}

/**
 * 参与者结算
 */
interface ParticipantResult {
    val playerId: PlayerId
    val damage: Long
    val healing: Long
    val deaths: Int
    val contribution: Double
    val rewards: List<RewardItem>
}

/**
 * 奖励物品
 */
interface RewardItem {
    val itemId: String
    val amount: Int
    val metadata: Map<String, Any>
}

/**
 * 匹配请求
 */
interface MatchmakingRequest {
    val playerId: PlayerId
    val templateId: String
    val difficulty: DungeonDifficulty
    val partyIds: List<PlayerId>
    val preferences: MatchmakingPreferences
}

/**
 * 匹配偏好
 */
interface MatchmakingPreferences {
    val maxWaitTime: Duration
    val acceptCrossServer: Boolean
    val preferSameLanguage: Boolean
}

/**
 * 匹配结果
 */
interface MatchmakingResult {
    val status: MatchmakingStatus
    val matchId: String?
    val estimatedWaitTime: Duration?
    val instanceId: String?
    val message: String?
}

/**
 * 副本记录
 */
interface DungeonRecord {
    val recordId: String
    val playerId: PlayerId
    val templateId: String
    val difficulty: DungeonDifficulty
    val cleared: Boolean
    val duration: Duration
    val score: Int
    val rating: DungeonRating
    val completedAt: Instant
}

/**
 * 排行榜条目
 */
interface LeaderboardEntry {
    val rank: Int
    val playerId: PlayerId
    val playerName: String
    val score: Int
    val duration: Duration
    val rating: DungeonRating
    val achievedAt: Instant
}

/**
 * 排行榜类型
 */
enum class LeaderboardType {
    FASTEST_CLEAR,
    HIGHEST_SCORE,
    MOST_CLEARS
}

/**
 * 副本服务接口
 */
interface DungeonService {
    /**
     * 获取副本模板列表
     */
    suspend fun listTemplates(
        minLevel: Int? = null,
        maxLevel: Int? = null,
        difficulty: DungeonDifficulty? = null
    ): Result<List<DungeonTemplateInfo>, DungeonError>

    /**
     * 获取副本模板详情
     */
    suspend fun getTemplate(templateId: String): Result<DungeonTemplateInfo, DungeonError>

    /**
     * 创建副本实例
     */
    suspend fun createInstance(
        templateId: String,
        difficulty: DungeonDifficulty,
        leaderId: PlayerId,
        memberIds: List<PlayerId>
    ): Result<DungeonInstanceInfo, DungeonError>

    /**
     * 获取副本实例信息
     */
    suspend fun getInstance(instanceId: String): Result<DungeonInstanceInfo, DungeonError>

    /**
     * 加入副本
     */
    suspend fun joinInstance(instanceId: String, playerId: PlayerId): Result<JoinResult, DungeonError>

    /**
     * 离开副本
     */
    suspend fun leaveInstance(instanceId: String, playerId: PlayerId): Result<Unit, DungeonError>

    /**
     * 获取副本进度
     */
    suspend fun getProgress(instanceId: String): Result<DungeonProgress, DungeonError>

    /**
     * 完成副本
     */
    suspend fun completeInstance(instanceId: String, result: DungeonResult): Result<Unit, DungeonError>
}

/**
 * 加入结果
 */
interface JoinResult {
    val success: Boolean
    val instanceId: String
    val worldName: String
    val spawnPosition: SpawnPosition
}

/**
 * 出生点位置
 */
interface SpawnPosition {
    val x: Double
    val y: Double
    val z: Double
}

/**
 * 匹配服务接口
 */
interface MatchmakingService {
    /**
     * 加入匹配队列
     */
    suspend fun joinQueue(request: MatchmakingRequest): Result<MatchmakingResult, DungeonError>

    /**
     * 离开匹配队列
     */
    suspend fun leaveQueue(playerId: PlayerId): Result<Unit, DungeonError>

    /**
     * 获取匹配状态
     */
    suspend fun getQueueStatus(playerId: PlayerId): Result<MatchmakingResult, DungeonError>

    /**
     * 确认匹配
     */
    suspend fun confirmMatch(playerId: PlayerId, matchId: String): Result<Unit, DungeonError>

    /**
     * 拒绝匹配
     */
    suspend fun declineMatch(playerId: PlayerId, matchId: String): Result<Unit, DungeonError>
}

/**
 * 副本记录服务接口
 */
interface DungeonRecordService {
    /**
     * 获取玩家副本记录
     */
    suspend fun getPlayerRecords(
        playerId: PlayerId,
        templateId: String? = null,
        limit: Int = 20
    ): Result<List<DungeonRecord>, DungeonError>

    /**
     * 获取排行榜
     */
    suspend fun getLeaderboard(
        templateId: String,
        difficulty: DungeonDifficulty,
        type: LeaderboardType,
        limit: Int = 100
    ): Result<List<LeaderboardEntry>, DungeonError>

    /**
     * 获取玩家排名
     */
    suspend fun getPlayerRank(
        playerId: PlayerId,
        templateId: String,
        difficulty: DungeonDifficulty,
        type: LeaderboardType
    ): Result<LeaderboardEntry?, DungeonError>

    /**
     * 检查每日入场次数
     */
    suspend fun checkDailyEntries(
        playerId: PlayerId,
        templateId: String
    ): Result<EntryCheckResult, DungeonError>
}

/**
 * 入场检查结果
 */
interface EntryCheckResult {
    val canEnter: Boolean
    val usedEntries: Int
    val maxEntries: Int
    val resetAt: Instant
}

/**
 * 副本错误类型
 */
sealed class DungeonError(val message: String) {
    class TemplateNotFound(templateId: String) : DungeonError("Template not found: $templateId")
    class InstanceNotFound(instanceId: String) : DungeonError("Instance not found: $instanceId")
    class InstanceFull(instanceId: String) : DungeonError("Instance is full: $instanceId")
    class InstanceClosed(instanceId: String) : DungeonError("Instance is closed: $instanceId")
    class PlayerAlreadyInInstance(playerId: PlayerId) : DungeonError("Player already in instance: $playerId")
    class PlayerNotInInstance(playerId: PlayerId) : DungeonError("Player not in instance: $playerId")
    class LevelTooLow(required: Int, actual: Int) : DungeonError("Level too low: required $required, actual $actual")
    class DailyLimitReached(templateId: String) : DungeonError("Daily entry limit reached: $templateId")
    class WeeklyLimitReached(templateId: String) : DungeonError("Weekly entry limit reached: $templateId")
    class MatchmakingFailed(reason: String) : DungeonError("Matchmaking failed: $reason")
    class AlreadyInQueue(playerId: PlayerId) : DungeonError("Player already in queue: $playerId")
    class NotInQueue(playerId: PlayerId) : DungeonError("Player not in queue: $playerId")
    class MatchExpired(matchId: String) : DungeonError("Match expired: $matchId")
    class InternalError(cause: String) : DungeonError("Internal error: $cause")
}
