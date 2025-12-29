package com.azathoth.services.dungeon.model

import java.time.Duration

/**
 * 副本模板信息
 */
interface DungeonTemplateInfo {
    val templateId: String
    val name: String
    val description: String
    val minPlayers: Int
    val maxPlayers: Int
    val recommendedLevel: Int
    val minLevel: Int
    val supportedDifficulties: List<DungeonDifficulty>
    val timeLimit: Duration
    val dailyEntryLimit: Int
    val weeklyEntryLimit: Int
}
