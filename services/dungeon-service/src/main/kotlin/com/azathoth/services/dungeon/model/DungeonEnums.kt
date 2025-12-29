package com.azathoth.services.dungeon.model

/**
 * 副本难度
 */
enum class DungeonDifficulty {
    NORMAL,
    HARD,
    NIGHTMARE,
    HELL
}

/**
 * 副本状态
 */
enum class DungeonInstanceState {
    CREATING,
    WAITING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CLOSED
}

/**
 * 副本评级
 */
enum class DungeonRating {
    F, D, C, B, A, S
}

/**
 * 排行榜类型
 */
enum class LeaderboardType {
    FASTEST_CLEAR,
    HIGHEST_SCORE,
    MOST_CLEARS
}
