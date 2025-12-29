package com.azathoth.services.dungeon.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Duration

/**
 * 匹配状态
 */
enum class MatchmakingStatus {
    QUEUED,
    MATCHING,
    FOUND,
    CANCELLED,
    TIMEOUT
}

/**
 * 匹配请求
 */
interface MatchmakingRequest {
    val playerId: PlayerId
    val templateId: String
    val difficulty: DungeonDifficulty
    val partyIds: List<PlayerId>
    val preferences: MatchmakingPreferences
}

/**
 * 匹配偏好
 */
interface MatchmakingPreferences {
    val maxWaitTime: Duration
    val acceptCrossServer: Boolean
    val preferSameLanguage: Boolean
}

/**
 * 匹配结果
 */
interface MatchmakingResult {
    val status: MatchmakingStatus
    val matchId: String?
    val estimatedWaitTime: Duration?
    val instanceId: String?
    val message: String?
}
