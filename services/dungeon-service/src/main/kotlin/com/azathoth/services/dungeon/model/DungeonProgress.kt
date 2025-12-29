package com.azathoth.services.dungeon.model

/**
 * 副本进度
 */
interface DungeonProgress {
    val instanceId: String
    val currentPhase: Int
    val totalPhases: Int
    val monstersKilled: Int
    val bossesKilled: Int
    val deaths: Int
    val score: Int
    val objectives: List<ObjectiveProgress>
}

/**
 * 目标进度
 */
interface ObjectiveProgress {
    val objectiveId: String
    val name: String
    val description: String
    val current: Int
    val target: Int
    val completed: Boolean
}
