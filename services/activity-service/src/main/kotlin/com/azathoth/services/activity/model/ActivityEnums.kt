package com.azathoth.services.activity.model

/**
 * 活动状态
 */
enum class ActivityState {
    SCHEDULED,
    PREPARING,
    ACTIVE,
    ENDING,
    ENDED,
    CANCELLED
}

/**
 * 活动类型
 */
enum class ActivityType {
    LIMITED_TIME,      // 限时活动
    RECURRING,         // 周期活动
    PERMANENT,         // 常驻活动
    SPECIAL_EVENT,     // 特殊事件
    SEASONAL           // 季节活动
}

/**
 * 任务状态
 */
enum class QuestState {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    CLAIMED
}

/**
 * 任务类型
 */
enum class QuestType {
    DAILY,
    WEEKLY,
    MAIN,
    SIDE,
    EVENT,
    ACHIEVEMENT
}

/**
 * 成就类型
 */
enum class AchievementCategory {
    COMBAT,
    EXPLORATION,
    COLLECTION,
    SOCIAL,
    DUNGEON,
    CRAFTING,
    ECONOMY,
    SPECIAL
}
