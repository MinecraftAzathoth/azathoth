package com.azathoth.services.activity.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Instant

/**
 * 成就信息
 */
interface AchievementInfo {
    val achievementId: String
    val name: String
    val description: String
    val category: AchievementCategory
    val points: Int
    val icon: String
    val hidden: Boolean
    val tiers: List<AchievementTier>
}

/**
 * 成就等级
 */
interface AchievementTier {
    val tier: Int
    val name: String
    val threshold: Int
    val rewards: List<QuestReward>
}

/**
 * 玩家成就进度
 */
interface PlayerAchievementProgress {
    val playerId: PlayerId
    val achievementId: String
    val currentProgress: Int
    val currentTier: Int
    val completedTiers: List<Int>
    val unlockedAt: Instant?
}

/**
 * 成就更新结果
 */
interface AchievementUpdateResult {
    val achievementId: String
    val newProgress: Int
    val tierUnlocked: Int?
    val completed: Boolean
    val rewards: List<QuestReward>?
}
