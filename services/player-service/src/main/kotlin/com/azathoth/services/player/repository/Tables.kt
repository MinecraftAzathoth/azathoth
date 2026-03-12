package com.azathoth.services.player.repository

import org.jetbrains.exposed.v1.core.Table

/**
 * 玩家表
 */
object Players : Table("players") {
    val playerId = varchar("player_id", 36)
    val username = varchar("username", 64).uniqueIndex()
    val displayName = varchar("display_name", 64)
    val level = integer("level").default(1)
    val experience = long("experience").default(0)
    val gold = long("gold").default(0)
    val diamond = long("diamond").default(0)
    val vipLevel = integer("vip_level").default(0)
    val firstLoginAt = long("first_login_at")
    val lastLoginAt = long("last_login_at")
    val totalOnlineTime = long("total_online_time").default(0)
    val isBanned = bool("is_banned").default(false)
    val banReason = varchar("ban_reason", 512).nullable()
    val banExpireAt = long("ban_expire_at").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(playerId)
}

/**
 * 玩家统计表
 */
object PlayerStatsTable : Table("player_stats") {
    val playerId = varchar("player_id", 36).references(Players.playerId)
    val mobsKilled = long("mobs_killed").default(0)
    val playersKilled = long("players_killed").default(0)
    val deaths = long("deaths").default(0)
    val dungeonsCompleted = long("dungeons_completed").default(0)
    val questsCompleted = long("quests_completed").default(0)
    val achievementsUnlocked = integer("achievements_unlocked").default(0)
    val distanceTraveled = long("distance_traveled").default(0)
    val blocksMined = long("blocks_mined").default(0)
    val blocksPlaced = long("blocks_placed").default(0)

    override val primaryKey = PrimaryKey(playerId)
}
