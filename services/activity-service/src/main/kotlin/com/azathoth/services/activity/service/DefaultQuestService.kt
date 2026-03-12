package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.activity.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class DefaultQuestService : QuestService {

    private val quests = ConcurrentHashMap<String, SimpleQuestInfo>()
    // key: "playerId:questId"
    private val playerQuests = ConcurrentHashMap<String, SimplePlayerQuestProgress>()

    fun registerQuest(quest: SimpleQuestInfo) {
        quests[quest.questId] = quest
        logger.info { "注册任务: ${quest.name} (${quest.questId})" }
    }

    override suspend fun listAvailableQuests(
        playerId: PlayerId,
        type: QuestType?
    ): Result<List<QuestInfo>> {
        val available = quests.values.filter { q ->
            (type == null || q.type == type) &&
                q.prerequisites.all { prereq ->
                    val key = "${playerId.value}:$prereq"
                    playerQuests[key]?.state == QuestState.CLAIMED
                }
        }
        return Result.success(available)
    }

    override suspend fun getQuest(questId: String): Result<QuestInfo> {
        val quest = quests[questId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "任务不存在: $questId")
        return Result.success(quest)
    }

    override suspend fun getPlayerQuestProgress(
        playerId: PlayerId,
        questId: String
    ): Result<PlayerQuestProgress> {
        val key = "${playerId.value}:$questId"
        val progress = playerQuests[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "任务进度不存在")
        return Result.success(progress)
    }

    override suspend fun getActiveQuests(playerId: PlayerId): Result<List<PlayerQuestProgress>> {
        val active = playerQuests.values.filter {
            it.playerId == playerId && it.state == QuestState.IN_PROGRESS
        }
        return Result.success(active)
    }

    override suspend fun acceptQuest(
        playerId: PlayerId,
        questId: String
    ): Result<PlayerQuestProgress> {
        quests[questId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "任务不存在: $questId")

        val key = "${playerId.value}:$questId"
        if (playerQuests.containsKey(key)) {
            val existing = playerQuests[key]!!
            if (existing.state != QuestState.AVAILABLE && existing.state != QuestState.LOCKED) {
                return Result.failure(ErrorCodes.ALREADY_EXISTS, "任务已接受或已完成")
            }
        }

        val progress = SimplePlayerQuestProgress(
            playerId = playerId,
            questId = questId,
            state = QuestState.IN_PROGRESS,
            objectiveProgress = emptyMap(),
            startedAt = Instant.now()
        )
        playerQuests[key] = progress
        logger.info { "玩家 $playerId 接受任务 $questId" }
        return Result.success(progress)
    }

    override suspend fun abandonQuest(
        playerId: PlayerId,
        questId: String
    ): Result<Unit> {
        val key = "${playerId.value}:$questId"
        val progress = playerQuests[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "任务进度不存在")

        if (progress.state != QuestState.IN_PROGRESS) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "只能放弃进行中的任务")
        }

        playerQuests[key] = progress.copy(state = QuestState.AVAILABLE)
        logger.info { "玩家 $playerId 放弃任务 $questId" }
        return Result.success(Unit)
    }

    override suspend fun updateQuestProgress(
        playerId: PlayerId,
        questId: String,
        objectiveId: String,
        progressDelta: Int
    ): Result<PlayerQuestProgress> {
        val key = "${playerId.value}:$questId"
        val progress = playerQuests[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "任务进度不存在")

        if (progress.state != QuestState.IN_PROGRESS) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "任务不在进行中")
        }

        val currentVal = progress.objectiveProgress.getOrDefault(objectiveId, 0)
        val updated = progress.copy(
            objectiveProgress = progress.objectiveProgress + (objectiveId to (currentVal + progressDelta))
        )

        // 检查是否所有目标完成
        val quest = quests[questId]!!
        val allCompleted = quest.objectives.all { obj ->
            (updated.objectiveProgress[obj.objectiveId] ?: 0) >= obj.target
        }

        val finalProgress = if (allCompleted) {
            updated.copy(state = QuestState.COMPLETED, completedAt = Instant.now())
        } else {
            updated
        }

        playerQuests[key] = finalProgress
        return Result.success(finalProgress)
    }

    override suspend fun completeQuest(
        playerId: PlayerId,
        questId: String
    ): Result<QuestCompletionResult> {
        val key = "${playerId.value}:$questId"
        val progress = playerQuests[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "任务进度不存在")

        if (progress.state != QuestState.COMPLETED) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "任务未完成")
        }

        val quest = quests[questId]!!
        val result = SimpleQuestCompletionResult(
            questId = questId,
            completed = true,
            rewards = quest.rewards
        )
        logger.info { "玩家 $playerId 完成任务 $questId" }
        return Result.success(result)
    }

    override suspend fun claimQuestRewards(
        playerId: PlayerId,
        questId: String
    ): Result<ClaimResult> {
        val key = "${playerId.value}:$questId"
        val progress = playerQuests[key]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "任务进度不存在")

        if (progress.state != QuestState.COMPLETED) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "任务未完成，无法领取奖励")
        }

        playerQuests[key] = progress.copy(state = QuestState.CLAIMED)

        val quest = quests[questId]!!
        val claimed = quest.rewards.map { SimpleClaimedReward(it.itemId, it.amount) }
        logger.info { "玩家 $playerId 领取任务 $questId 奖励" }
        return Result.success(SimpleClaimResult(success = true, rewards = claimed))
    }

    override suspend fun refreshDailyQuests(playerId: PlayerId): Result<List<QuestInfo>> {
        // 重置所有每日任务进度
        val dailyKeys = playerQuests.keys().toList().filter { key ->
            val progress = playerQuests[key] ?: return@filter false
            progress.playerId == playerId && quests[progress.questId]?.type == QuestType.DAILY
        }

        for (key in dailyKeys) {
            playerQuests.remove(key)
        }

        val dailyQuests = quests.values.filter { it.type == QuestType.DAILY }
        logger.info { "刷新玩家 $playerId 的每日任务, 共 ${dailyQuests.size} 个" }
        return Result.success(dailyQuests)
    }
}
