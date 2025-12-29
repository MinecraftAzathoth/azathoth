package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.*

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
    ): Result<List<QuestInfo>>

    /**
     * 获取任务详情
     */
    suspend fun getQuest(questId: String): Result<QuestInfo>

    /**
     * 获取玩家任务进度
     */
    suspend fun getPlayerQuestProgress(
        playerId: PlayerId,
        questId: String
    ): Result<PlayerQuestProgress>

    /**
     * 获取玩家所有进行中的任务
     */
    suspend fun getActiveQuests(playerId: PlayerId): Result<List<PlayerQuestProgress>>

    /**
     * 接受任务
     */
    suspend fun acceptQuest(
        playerId: PlayerId,
        questId: String
    ): Result<PlayerQuestProgress>

    /**
     * 放弃任务
     */
    suspend fun abandonQuest(
        playerId: PlayerId,
        questId: String
    ): Result<Unit>

    /**
     * 更新任务进度
     */
    suspend fun updateQuestProgress(
        playerId: PlayerId,
        questId: String,
        objectiveId: String,
        progressDelta: Int
    ): Result<PlayerQuestProgress>

    /**
     * 完成任务
     */
    suspend fun completeQuest(
        playerId: PlayerId,
        questId: String
    ): Result<QuestCompletionResult>

    /**
     * 领取任务奖励
     */
    suspend fun claimQuestRewards(
        playerId: PlayerId,
        questId: String
    ): Result<ClaimResult>

    /**
     * 刷新每日任务
     */
    suspend fun refreshDailyQuests(playerId: PlayerId): Result<List<QuestInfo>>
}
