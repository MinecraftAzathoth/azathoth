package com.azathoth.services.dungeon.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Duration
import java.time.Instant

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
