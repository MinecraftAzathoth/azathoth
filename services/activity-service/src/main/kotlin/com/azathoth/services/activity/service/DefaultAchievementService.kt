package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class DefaultAchievementService : AchievementService {

    private val achievements = ConcurrentHashMap<String, SimpleAchievementInfo>()
    // key: "playerId:achievementId"
    private val playerAchievements = ConcurrentHashMap<String, SimplePlayerAchievementProgress>()

    fun registerAchievement(achievement: SimpleAchievementInfo) {
        achievements[achievement.achievementId] = achievement
        logger.info { "注册成就: ${achievement.name} (${achievement.achievementId})" }
    }

    override suspend fun listAchievements(
        category: AchievementCategory?
    ): Result<List<AchievementInfo>> {
        val filtered = achievements.values.filter { a ->
            category == null || a.category == category
        }
        return Result.success(filtered.toList())
    }

    override suspend fun getAchievement(achievementId: String): Result<AchievementInfo> {
        val achievement = achievements[achievementId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "成就不存在: $achievementId")
        return Result.success(achievement)
    }

    override suspend fun getPlayerAchievementProgress(
        playerId: PlayerId,
        achievementId: String
    ): Result<PlayerAchievementProgress> {
        val key = "${playerId.value}:$achievementId"
        val progress = playerAchievements[key]
            ?: return Result.success(
                SimplePlayerAchievementProgress(
                    playerId = playerId,
                    achievementId = achievementId,
                    currentProgress = 0,
                    currentTier = 0
                )
            )
        return Result.success(progress)
    }

    override suspend fun getPlayerAchievements(
        playerId: PlayerId,
        category: AchievementCategory?
    ): Result<List<PlayerAchievementProgress>> {
        val filtered = playerAchievements.values.filter { p ->
            p.playerId == playerId &&
                (category == null || achievements[p.achievementId]?.category == category)
        }
        return Result.success(filtered.toList())
    }

    override suspend fun updateAchievementProgress(
        playerId: PlayerId,
        achievementId: String,
        progressDelta: Int
    ): Result<AchievementUpdateResult> {
        val achievement = achievements[achievementId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "成就不存在: $achievementId")

        val key = "${playerId.value}:$achievementId"
        val current = playerAchievements[key] ?: SimplePlayerAchievementProgress(
            playerId = playerId,
            achievementId = achievementId,
            currentProgress = 0,
            currentTier = 0
        )

        val newProgress = current.currentProgress + progressDelta

        // 检查是否解锁新等级
        var newTier = current.currentTier
        var tierUnlocked: Int? = null
        val newCompletedTiers = current.completedTiers.toMutableList()
        var rewards: List<QuestReward>? = null

        for (tier in achievement.tiers) {
            if (tier.tier > current.currentTier && newProgress >= tier.threshold) {
                newTier = tier.tier
                tierUnlocked = tier.tier
                newCompletedTiers.add(tier.tier)
                rewards = tier.rewards
            }
        }

        val maxTier = achievement.tiers.maxOfOrNull { it.tier } ?: 0
        val completed = newTier >= maxTier && maxTier > 0

        val updated = current.copy(
            currentProgress = newProgress,
            currentTier = newTier,
            completedTiers = newCompletedTiers,
            unlockedAt = if (completed && current.unlockedAt == null) Instant.now() else current.unlockedAt
        )
        playerAchievements[key] = updated

        val result = SimpleAchievementUpdateResult(
            achievementId = achievementId,
            newProgress = newProgress,
            tierUnlocked = tierUnlocked,
            completed = completed,
            rewards = rewards
        )

        if (tierUnlocked != null) {
            logger.info { "玩家 $playerId 解锁成就 $achievementId 等级 $tierUnlocked" }
        }
        return Result.success(result)
    }

    override suspend fun getAchievementPoints(playerId: PlayerId): Result<Int> {
        val totalPoints = playerAchievements.values
            .filter { it.playerId == playerId }
            .sumOf { progress ->
                val achievement = achievements[progress.achievementId]
                if (achievement != null) {
                    progress.completedTiers.sumOf { tier ->
                        achievement.tiers.find { it.tier == tier }?.let { achievement.points } ?: 0
                    }
                } else 0
            }
        return Result.success(totalPoints)
    }
}
