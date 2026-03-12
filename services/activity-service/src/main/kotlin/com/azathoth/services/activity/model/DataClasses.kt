package com.azathoth.services.activity.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Duration
import java.time.Instant

data class SimpleActivityInfo(
    override val activityId: String,
    override val name: String,
    override val description: String,
    override val type: ActivityType,
    override val state: ActivityState,
    override val startTime: Instant,
    override val endTime: Instant,
    override val config: ActivityConfig,
    override val rewards: List<ActivityReward>
) : ActivityInfo

data class SimpleActivityConfig(
    override val minLevel: Int = 1,
    override val maxParticipants: Int? = null,
    override val repeatInterval: Duration? = null,
    override val rules: Map<String, Any> = emptyMap()
) : ActivityConfig

data class SimpleActivityReward(
    override val rewardId: String,
    override val name: String,
    override val description: String,
    override val itemId: String,
    override val amount: Int,
    override val condition: RewardCondition
) : ActivityReward

data class SimpleRewardCondition(
    override val type: String,
    override val threshold: Int,
    override val parameters: Map<String, Any> = emptyMap()
) : RewardCondition

data class SimplePlayerActivityProgress(
    override val playerId: PlayerId,
    override val activityId: String,
    override val score: Long,
    override val rank: Int? = null,
    override val completedObjectives: List<String> = emptyList(),
    override val claimedRewards: List<String> = emptyList(),
    override val joinedAt: Instant,
    override val lastUpdatedAt: Instant
) : PlayerActivityProgress

data class SimpleActivityLeaderboardEntry(
    override val rank: Int,
    override val playerId: PlayerId,
    override val playerName: String,
    override val score: Long,
    override val updatedAt: Instant
) : ActivityLeaderboardEntry

data class SimpleClaimResult(
    override val success: Boolean,
    override val rewards: List<ClaimedReward>
) : ClaimResult

data class SimpleClaimedReward(
    override val itemId: String,
    override val amount: Int
) : ClaimedReward

// Quest data classes

data class SimpleQuestInfo(
    override val questId: String,
    override val name: String,
    override val description: String,
    override val type: QuestType,
    override val objectives: List<QuestObjective>,
    override val rewards: List<QuestReward>,
    override val prerequisites: List<String> = emptyList(),
    override val expiresAt: Instant? = null
) : QuestInfo

data class SimpleQuestObjective(
    override val objectiveId: String,
    override val description: String,
    override val type: String,
    override val target: Int,
    override val parameters: Map<String, Any> = emptyMap()
) : QuestObjective

data class SimpleQuestReward(
    override val itemId: String,
    override val amount: Int,
    override val experience: Long = 0,
    override val currency: Map<String, Long> = emptyMap()
) : QuestReward

data class SimplePlayerQuestProgress(
    override val playerId: PlayerId,
    override val questId: String,
    override val state: QuestState,
    override val objectiveProgress: Map<String, Int> = emptyMap(),
    override val startedAt: Instant? = null,
    override val completedAt: Instant? = null
) : PlayerQuestProgress

data class SimpleQuestCompletionResult(
    override val questId: String,
    override val completed: Boolean,
    override val rewards: List<QuestReward>
) : QuestCompletionResult

// Achievement data classes

data class SimpleAchievementInfo(
    override val achievementId: String,
    override val name: String,
    override val description: String,
    override val category: AchievementCategory,
    override val points: Int,
    override val icon: String,
    override val hidden: Boolean = false,
    override val tiers: List<AchievementTier>
) : AchievementInfo

data class SimpleAchievementTier(
    override val tier: Int,
    override val name: String,
    override val threshold: Int,
    override val rewards: List<QuestReward>
) : AchievementTier

data class SimplePlayerAchievementProgress(
    override val playerId: PlayerId,
    override val achievementId: String,
    override val currentProgress: Int,
    override val currentTier: Int,
    override val completedTiers: List<Int> = emptyList(),
    override val unlockedAt: Instant? = null
) : PlayerAchievementProgress

data class SimpleAchievementUpdateResult(
    override val achievementId: String,
    override val newProgress: Int,
    override val tierUnlocked: Int? = null,
    override val completed: Boolean = false,
    override val rewards: List<QuestReward>? = null
) : AchievementUpdateResult
