package com.azathoth.services.dungeon.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Duration
import java.time.Instant

/**
 * 副本记录
 */
interface DungeonRecord {
    val recordId: String
    val playerId: PlayerId
    val templateId: String
    val difficulty: DungeonDifficulty
    val cleared: Boolean
    val duration: Duration
    val score: Int
    val rating: DungeonRating
    val completedAt: Instant
}

/**
 * 排行榜条目
 */
interface LeaderboardEntry {
    val rank: Int
    val playerId: PlayerId
    val playerName: String
    val score: Int
    val duration: Duration
    val rating: DungeonRating
    val achievedAt: Instant
}

/**
 * 入场检查结果
 */
interface EntryCheckResult {
    val canEnter: Boolean
    val usedEntries: Int
    val maxEntries: Int
    val resetAt: Instant
}
