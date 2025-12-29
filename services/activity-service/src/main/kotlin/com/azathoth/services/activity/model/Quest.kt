package com.azathoth.services.activity.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Instant

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
 * 任务完成结果
 */
interface QuestCompletionResult {
    val questId: String
    val completed: Boolean
    val rewards: List<QuestReward>
}
