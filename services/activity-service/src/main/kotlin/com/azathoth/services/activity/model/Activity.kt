package com.azathoth.services.activity.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Duration
import java.time.Instant

/**
 * 活动信息
 */
interface ActivityInfo {
    val activityId: String
    val name: String
    val description: String
    val type: ActivityType
    val state: ActivityState
    val startTime: Instant
    val endTime: Instant
    val config: ActivityConfig
    val rewards: List<ActivityReward>
}

/**
 * 活动配置
 */
interface ActivityConfig {
    val minLevel: Int
    val maxParticipants: Int?
    val repeatInterval: Duration?
    val rules: Map<String, Any>
}

/**
 * 活动奖励
 */
interface ActivityReward {
    val rewardId: String
    val name: String
    val description: String
    val itemId: String
    val amount: Int
    val condition: RewardCondition
}

/**
 * 奖励条件
 */
interface RewardCondition {
    val type: String
    val threshold: Int
    val parameters: Map<String, Any>
}

/**
 * 玩家活动进度
 */
interface PlayerActivityProgress {
    val playerId: PlayerId
    val activityId: String
    val score: Long
    val rank: Int?
    val completedObjectives: List<String>
    val claimedRewards: List<String>
    val joinedAt: Instant
    val lastUpdatedAt: Instant
}

/**
 * 活动排行榜条目
 */
interface ActivityLeaderboardEntry {
    val rank: Int
    val playerId: PlayerId
    val playerName: String
    val score: Long
    val updatedAt: Instant
}

/**
 * 领取结果
 */
interface ClaimResult {
    val success: Boolean
    val rewards: List<ClaimedReward>
}

/**
 * 已领取奖励
 */
interface ClaimedReward {
    val itemId: String
    val amount: Int
}
