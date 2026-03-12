package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class DefaultActivityService : ActivityService {

    private val activities = ConcurrentHashMap<String, SimpleActivityInfo>()
    // key: "playerId:activityId"
    private val playerProgress = ConcurrentHashMap<String, SimplePlayerActivityProgress>()

    fun registerActivity(activity: SimpleActivityInfo) {
        activities[activity.activityId] = activity
        logger.info { "注册活动: ${activity.name} (${activity.activityId})" }
    }

    override suspend fun listActivities(
        state: ActivityState?,
        type: ActivityType?
    ): Result<List<ActivityInfo>> {
        val filtered = activities.values.filter { a ->
            (state == null || a.state == state) && (type == null || a.type == type)
        }
        return Result.success(filtered)
    }

    override suspend fun getActivity(activityId: String): Result<ActivityInfo> {
        val activity = activities[activityId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "活动不存在: $activityId")
        return Result.success(activity)
    }

    override suspend fun getPlayerProgress(
        playerId: PlayerId,
        activityId: String
    ): Result<PlayerActivityProgress> {
        val key = "${playerId.value}:$activityId"
        val progress = playerProgress[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家活动进度不存在")
        return Result.success(progress)
    }

    override suspend fun joinActivity(
        playerId: PlayerId,
        activityId: String
    ): Result<PlayerActivityProgress> {
        val activity = activities[activityId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "活动不存在: $activityId")

        if (activity.state != ActivityState.ACTIVE) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "活动未开启")
        }

        val key = "${playerId.value}:$activityId"
        if (playerProgress.containsKey(key)) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "已参加该活动")
        }

        val now = Instant.now()
        val progress = SimplePlayerActivityProgress(
            playerId = playerId,
            activityId = activityId,
            score = 0,
            joinedAt = now,
            lastUpdatedAt = now
        )
        playerProgress[key] = progress
        logger.info { "玩家 $playerId 加入活动 $activityId" }
        return Result.success(progress)
    }

    override suspend fun updateProgress(
        playerId: PlayerId,
        activityId: String,
        progressDelta: Long
    ): Result<PlayerActivityProgress> {
        val key = "${playerId.value}:$activityId"
        val current = playerProgress[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家未参加该活动")

        val updated = current.copy(
            score = current.score + progressDelta,
            lastUpdatedAt = Instant.now()
        )
        playerProgress[key] = updated
        return Result.success(updated)
    }

    override suspend fun claimReward(
        playerId: PlayerId,
        activityId: String,
        rewardId: String
    ): Result<ClaimResult> {
        val key = "${playerId.value}:$activityId"
        val progress = playerProgress[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家未参加该活动")

        if (rewardId in progress.claimedRewards) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "奖励已领取")
        }

        val activity = activities[activityId]!!
        val reward = activity.rewards.find { it.rewardId == rewardId }
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "奖励不存在: $rewardId")

        if (progress.score < reward.condition.threshold) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "未达到领取条件")
        }

        playerProgress[key] = progress.copy(
            claimedRewards = progress.claimedRewards + rewardId
        )

        val result = SimpleClaimResult(
            success = true,
            rewards = listOf(SimpleClaimedReward(reward.itemId, reward.amount))
        )
        logger.info { "玩家 $playerId 领取活动 $activityId 奖励 $rewardId" }
        return Result.success(result)
    }

    override suspend fun getLeaderboard(
        activityId: String,
        limit: Int,
        offset: Int
    ): Result<List<ActivityLeaderboardEntry>> {
        val entries = playerProgress.values
            .filter { it.activityId == activityId }
            .sortedByDescending { it.score }
            .drop(offset)
            .take(limit)
            .mapIndexed { index, p ->
                SimpleActivityLeaderboardEntry(
                    rank = offset + index + 1,
                    playerId = p.playerId,
                    playerName = p.playerId.value,
                    score = p.score,
                    updatedAt = p.lastUpdatedAt
                )
            }
        return Result.success(entries)
    }
}
