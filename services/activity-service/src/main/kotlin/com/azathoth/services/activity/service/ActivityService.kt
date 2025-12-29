package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.*

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
    ): Result<List<ActivityInfo>>

    /**
     * 获取活动详情
     */
    suspend fun getActivity(activityId: String): Result<ActivityInfo>

    /**
     * 获取玩家活动进度
     */
    suspend fun getPlayerProgress(
        playerId: PlayerId,
        activityId: String
    ): Result<PlayerActivityProgress>

    /**
     * 加入活动
     */
    suspend fun joinActivity(
        playerId: PlayerId,
        activityId: String
    ): Result<PlayerActivityProgress>

    /**
     * 更新活动进度
     */
    suspend fun updateProgress(
        playerId: PlayerId,
        activityId: String,
        progressDelta: Long
    ): Result<PlayerActivityProgress>

    /**
     * 领取活动奖励
     */
    suspend fun claimReward(
        playerId: PlayerId,
        activityId: String,
        rewardId: String
    ): Result<ClaimResult>

    /**
     * 获取活动排行榜
     */
    suspend fun getLeaderboard(
        activityId: String,
        limit: Int = 100,
        offset: Int = 0
    ): Result<List<ActivityLeaderboardEntry>>
}
