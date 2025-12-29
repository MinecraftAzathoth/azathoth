package com.azathoth.services.dungeon.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Duration
import java.time.Instant

/**
 * 副本实例信息
 */
interface DungeonInstanceInfo {
    val instanceId: String
    val templateId: String
    val difficulty: DungeonDifficulty
    val state: DungeonInstanceState
    val playerIds: Set<PlayerId>
    val leaderId: PlayerId
    val createdAt: Instant
    val startedAt: Instant?
    val completedAt: Instant?
    val elapsedTime: Duration
    val remainingTime: Duration?
    val currentPhase: Int
    val totalPhases: Int
}

/**
 * 加入结果
 */
interface JoinResult {
    val success: Boolean
    val instanceId: String
    val worldName: String
    val spawnPosition: SpawnPosition
}

/**
 * 出生点位置
 */
interface SpawnPosition {
    val x: Double
    val y: Double
    val z: Double
}
