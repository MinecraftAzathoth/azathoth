package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.*

/**
 * 成就服务接口
 */
interface AchievementService {
    /**
     * 获取成就列表
     */
    suspend fun listAchievements(
        category: AchievementCategory? = null
    ): Result<List<AchievementInfo>>

    /**
     * 获取成就详情
     */
    suspend fun getAchievement(achievementId: String): Result<AchievementInfo>

    /**
     * 获取玩家成就进度
     */
    suspend fun getPlayerAchievementProgress(
        playerId: PlayerId,
        achievementId: String
    ): Result<PlayerAchievementProgress>

    /**
     * 获取玩家所有成就
     */
    suspend fun getPlayerAchievements(
        playerId: PlayerId,
        category: AchievementCategory? = null
    ): Result<List<PlayerAchievementProgress>>

    /**
     * 更新成就进度
     */
    suspend fun updateAchievementProgress(
        playerId: PlayerId,
        achievementId: String,
        progressDelta: Int
    ): Result<AchievementUpdateResult>

    /**
     * 获取成就点数
     */
    suspend fun getAchievementPoints(playerId: PlayerId): Result<Int>
}
