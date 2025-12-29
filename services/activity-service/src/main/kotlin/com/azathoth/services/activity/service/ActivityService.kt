package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import java.time.Duration
import java.time.Instant

/**
 * 活动状态
 */
enum class ActivityState {
    SCHEDULED,
    PREPARING,
    ACTIVE,
    ENDING,
    ENDED,
    CANCELLED
}

/**
 * 活动类型
 */
enum class ActivityType {
    LIMITED_TIME,      // 限时活动
    RECURRING,         // 周期活动
    PERMANENT,         // 常驻活动
    SPECIAL_EVENT,     // 特殊事件
    SEASONAL           // 季节活动
}

/**
 * 任务状态
 */
enum class QuestState {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    CLAIMED
}

/**
 * 任务类型
 */
enum class QuestType {
    DAILY,
    WEEKLY,
    MAIN,
    SIDE,
    EVENT,
    ACHIEVEMENT
}

/**
 * 成就类型
 */
enum class AchievementCategory {
    COMBAT,
    EXPLORATION,
    COLLECTION,
    SOCIAL,
    DUNGEON,
    CRAFTING,
    ECONOMY,
    SPECIAL
}

/**
 * 活动信息
 */
interface ActivityInfo {
    val activityId: String
    val name: String
    val description: String
    val type: ActivityType
    val state: ActivityState
    val startTime: Instant
    val endTime: Instant
    val config: ActivityConfig
    val rewards: List<ActivityReward>
}

/**
 * 活动配置
 */
interface ActivityConfig {
    val minLevel: Int
    val maxParticipants: Int?
    val repeatInterval: Duration?
    val rules: Map<String, Any>
}

/**
 * 活动奖励
 */
interface ActivityReward {
    val rewardId: String
    val name: String
    val description: String
    val itemId: String
    val amount: Int
    val condition: RewardCondition
}

/**
 * 奖励条件
 */
interface RewardCondition {
    val type: String
    val threshold: Int
    val parameters: Map<String, Any>
}

/**
 * 玩家活动进度
 */
interface PlayerActivityProgress {
    val playerId: PlayerId
    val activityId: String
    val score: Long
    val rank: Int?
    val completedObjectives: List<String>
    val claimedRewards: List<String>
    val joinedAt: Instant
    val lastUpdatedAt: Instant
}

/**
 * 任务信息
 */
interface QuestInfo {
    val questId: String
    val name: String
    val description: String
    val type: QuestType
    val objectives: List<QuestObjective>
    val rewards: List<QuestReward>
    val prerequisites: List<String>
    val expiresAt: Instant?
}

/**
 * 任务目标
 */
interface QuestObjective {
    val objectiveId: String
    val description: String
    val type: String
    val target: Int
    val parameters: Map<String, Any>
}

/**
 * 任务奖励
 */
interface QuestReward {
    val itemId: String
    val amount: Int
    val experience: Long
    val currency: Map<String, Long>
}

/**
 * 玩家任务进度
 */
interface PlayerQuestProgress {
    val playerId: PlayerId
    val questId: String
    val state: QuestState
    val objectiveProgress: Map<String, Int>
    val startedAt: Instant?
    val completedAt: Instant?
}

/**
 * 成就信息
 */
interface AchievementInfo {
    val achievementId: String
    val name: String
    val description: String
    val category: AchievementCategory
    val points: Int
    val icon: String
    val hidden: Boolean
    val tiers: List<AchievementTier>
}

/**
 * 成就等级
 */
interface AchievementTier {
    val tier: Int
    val name: String
    val threshold: Int
    val rewards: List<QuestReward>
}

/**
 * 玩家成就进度
 */
interface PlayerAchievementProgress {
    val playerId: PlayerId
    val achievementId: String
    val currentProgress: Int
    val currentTier: Int
    val completedTiers: List<Int>
    val unlockedAt: Instant?
}

/**
 * 活动排行榜条目
 */
interface ActivityLeaderboardEntry {
    val rank: Int
    val playerId: PlayerId
    val playerName: String
    val score: Long
    val updatedAt: Instant
}

/**
 * 活动服务接口
 */
interface ActivityService {
    /**
     * 获取活动列表
     */
    suspend fun listActivities(
        state: ActivityState? = null,
        type: ActivityType? = null
    ): Result<List<ActivityInfo>, ActivityError>

    /**
     * 获取活动详情
     */
    suspend fun getActivity(activityId: String): Result<ActivityInfo, ActivityError>

    /**
     * 获取玩家活动进度
     */
    suspend fun getPlayerProgress(
        playerId: PlayerId,
        activityId: String
    ): Result<PlayerActivityProgress, ActivityError>

    /**
     * 加入活动
     */
    suspend fun joinActivity(
        playerId: PlayerId,
        activityId: String
    ): Result<PlayerActivityProgress, ActivityError>

    /**
     * 更新活动进度
     */
    suspend fun updateProgress(
        playerId: PlayerId,
        activityId: String,
        progressDelta: Long
    ): Result<PlayerActivityProgress, ActivityError>

    /**
     * 领取活动奖励
     */
    suspend fun claimReward(
        playerId: PlayerId,
        activityId: String,
        rewardId: String
    ): Result<ClaimResult, ActivityError>

    /**
     * 获取活动排行榜
     */
    suspend fun getLeaderboard(
        activityId: String,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<ActivityLeaderboardEntry>, ActivityError>
}

/**
 * 领取结果
 */
interface ClaimResult {
    val success: Boolean
    val rewards: List<ClaimedReward>
}

/**
 * 已领取奖励
 */
interface ClaimedReward {
    val itemId: String
    val amount: Int
}

/**
 * 任务服务接口
 */
interface QuestService {
    /**
     * 获取可用任务列表
     */
    suspend fun listAvailableQuests(
        playerId: PlayerId,
        type: QuestType? = null
    ): Result<List<QuestInfo>, ActivityError>

    /**
     * 获取任务详情
     */
    suspend fun getQuest(questId: String): Result<QuestInfo, ActivityError>

    /**
     * 获取玩家任务进度
     */
    suspend fun getPlayerQuestProgress(
        playerId: PlayerId,
        questId: String
    ): Result<PlayerQuestProgress, ActivityError>

    /**
     * 获取玩家所有进行中的任务
     */
    suspend fun getActiveQuests(playerId: PlayerId): Result<List<PlayerQuestProgress>, ActivityError>

    /**
     * 接受任务
     */
    suspend fun acceptQuest(
        playerId: PlayerId,
        questId: String
    ): Result<PlayerQuestProgress, ActivityError>

    /**
     * 放弃任务
     */
    suspend fun abandonQuest(
        playerId: PlayerId,
        questId: String
    ): Result<Unit, ActivityError>

    /**
     * 更新任务进度
     */
    suspend fun updateQuestProgress(
        playerId: PlayerId,
        questId: String,
        objectiveId: String,
        progressDelta: Int
    ): Result<PlayerQuestProgress, ActivityError>

    /**
     * 完成任务
     */
    suspend fun completeQuest(
        playerId: PlayerId,
        questId: String
    ): Result<QuestCompletionResult, ActivityError>

    /**
     * 领取任务奖励
     */
    suspend fun claimQuestRewards(
        playerId: PlayerId,
        questId: String
    ): Result<ClaimResult, ActivityError>

    /**
     * 刷新每日任务
     */
    suspend fun refreshDailyQuests(playerId: PlayerId): Result<List<QuestInfo>, ActivityError>
}

/**
 * 任务完成结果
 */
interface QuestCompletionResult {
    val questId: String
    val completed: Boolean
    val rewards: List<QuestReward>
}

/**
 * 成就服务接口
 */
interface AchievementService {
    /**
     * 获取成就列表
     */
    suspend fun listAchievements(
        category: AchievementCategory? = null
    ): Result<List<AchievementInfo>, ActivityError>

    /**
     * 获取成就详情
     */
    suspend fun getAchievement(achievementId: String): Result<AchievementInfo, ActivityError>

    /**
     * 获取玩家成就进度
     */
    suspend fun getPlayerAchievementProgress(
        playerId: PlayerId,
        achievementId: String
    ): Result<PlayerAchievementProgress, ActivityError>

    /**
     * 获取玩家所有成就
     */
    suspend fun getPlayerAchievements(
        playerId: PlayerId,
        category: AchievementCategory? = null
    ): Result<List<PlayerAchievementProgress>, ActivityError>

    /**
     * 更新成就进度
     */
    suspend fun updateAchievementProgress(
        playerId: PlayerId,
        achievementId: String,
        progressDelta: Int
    ): Result<AchievementUpdateResult, ActivityError>

    /**
     * 获取成就点数
     */
    suspend fun getAchievementPoints(playerId: PlayerId): Result<Int, ActivityError>
}

/**
 * 成就更新结果
 */
interface AchievementUpdateResult {
    val achievementId: String
    val newProgress: Int
    val tierUnlocked: Int?
    val completed: Boolean
    val rewards: List<QuestReward>?
}

/**
 * 活动调度服务接口
 */
interface ActivityScheduler {
    /**
     * 调度活动开始
     */
    suspend fun scheduleActivityStart(activityId: String, startTime: Instant): Result<Unit, ActivityError>

    /**
     * 调度活动结束
     */
    suspend fun scheduleActivityEnd(activityId: String, endTime: Instant): Result<Unit, ActivityError>

    /**
     * 取消调度
     */
    suspend fun cancelSchedule(activityId: String): Result<Unit, ActivityError>

    /**
     * 获取即将开始的活动
     */
    suspend fun getUpcomingActivities(within: Duration): Result<List<ActivityInfo>, ActivityError>
}

/**
 * 活动错误类型
 */
sealed class ActivityError(val message: String) {
    class ActivityNotFound(activityId: String) : ActivityError("Activity not found: $activityId")
    class ActivityNotActive(activityId: String) : ActivityError("Activity not active: $activityId")
    class ActivityEnded(activityId: String) : ActivityError("Activity has ended: $activityId")
    class AlreadyJoined(activityId: String) : ActivityError("Already joined activity: $activityId")
    class NotJoined(activityId: String) : ActivityError("Not joined activity: $activityId")
    class RewardAlreadyClaimed(rewardId: String) : ActivityError("Reward already claimed: $rewardId")
    class RewardConditionNotMet(rewardId: String) : ActivityError("Reward condition not met: $rewardId")
    class QuestNotFound(questId: String) : ActivityError("Quest not found: $questId")
    class QuestNotAvailable(questId: String) : ActivityError("Quest not available: $questId")
    class QuestAlreadyAccepted(questId: String) : ActivityError("Quest already accepted: $questId")
    class QuestNotInProgress(questId: String) : ActivityError("Quest not in progress: $questId")
    class QuestNotCompleted(questId: String) : ActivityError("Quest not completed: $questId")
    class PrerequisitesNotMet(questId: String) : ActivityError("Prerequisites not met: $questId")
    class AchievementNotFound(achievementId: String) : ActivityError("Achievement not found: $achievementId")
    class LevelTooLow(required: Int, actual: Int) : ActivityError("Level too low: required $required, actual $actual")
    class InternalError(cause: String) : ActivityError("Internal error: $cause")
}
