package com.azathoth.services.player.repository

import com.azathoth.core.common.database.DatabaseFactory
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

private val logger = KotlinLogging.logger {}

/**
 * PostgreSQL 玩家仓库实现
 *
 * 使用 Exposed ORM 操作 PostgreSQL 数据库。
 */
class PostgresPlayerRepository(
    private val db: DatabaseFactory
) : PlayerRepository {

    override suspend fun create(playerId: PlayerId, username: String): Result<PlayerEntity> = db.dbQuery {
        val existing = Players.selectAll().where { Players.playerId eq playerId.value }.singleOrNull()
        if (existing != null) {
            return@dbQuery Result.failure(ErrorCodes.ALREADY_EXISTS, "玩家已存在: ${playerId.value}")
        }

        val usernameExists = Players.selectAll().where { Players.username eq username }.singleOrNull()
        if (usernameExists != null) {
            return@dbQuery Result.failure(ErrorCodes.ALREADY_EXISTS, "用户名已存在: $username")
        }

        val now = System.currentTimeMillis()
        Players.insert {
            it[Players.playerId] = playerId.value
            it[Players.username] = username
            it[displayName] = username
            it[firstLoginAt] = now
            it[lastLoginAt] = now
            it[createdAt] = now
            it[updatedAt] = now
        }

        PlayerStatsTable.insert {
            it[PlayerStatsTable.playerId] = playerId.value
        }

        logger.info { "创建玩家: $username (${playerId.value})" }
        Result.success(findByIdInternal(playerId)!!)
    }

    override suspend fun findById(playerId: PlayerId): PlayerEntity? = db.dbQuery {
        findByIdInternal(playerId)
    }

    override suspend fun findByUsername(username: String): PlayerEntity? = db.dbQuery {
        Players.selectAll().where { Players.username eq username }
            .singleOrNull()?.toPlayerEntity()
    }

    override suspend fun save(player: PlayerEntity): Result<PlayerEntity> = db.dbQuery {
        val updated = Players.update({ Players.playerId eq player.playerId.value }) {
            it[displayName] = player.displayName
            it[level] = player.level
            it[experience] = player.experience
            it[gold] = player.gold
            it[diamond] = player.diamond
            it[vipLevel] = player.vipLevel
            it[lastLoginAt] = player.lastLoginAt
            it[totalOnlineTime] = player.totalOnlineTime
            it[isBanned] = player.isBanned
            it[banReason] = player.banReason
            it[banExpireAt] = player.banExpireAt
            it[updatedAt] = System.currentTimeMillis()
        }

        if (updated == 0) {
            return@dbQuery Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在: ${player.playerId.value}")
        }

        Result.success(findByIdInternal(player.playerId)!!)
    }

    override suspend fun delete(playerId: PlayerId): Result<Unit> = db.dbQuery {
        PlayerStatsTable.deleteWhere { PlayerStatsTable.playerId eq playerId.value }
        val deleted = Players.deleteWhere { Players.playerId eq playerId.value }
        if (deleted == 0) {
            Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在: ${playerId.value}")
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun exists(playerId: PlayerId): Boolean = db.dbQuery {
        Players.selectAll().where { Players.playerId eq playerId.value }.count() > 0
    }

    override suspend fun existsByUsername(username: String): Boolean = db.dbQuery {
        Players.selectAll().where { Players.username eq username }.count() > 0
    }

    override suspend fun getStats(playerId: PlayerId): PlayerStats? = db.dbQuery {
        PlayerStatsTable.selectAll().where { PlayerStatsTable.playerId eq playerId.value }
            .singleOrNull()?.toPlayerStats()
    }

    override suspend fun saveStats(stats: PlayerStats): Result<PlayerStats> = db.dbQuery {
        val updated = PlayerStatsTable.update({ PlayerStatsTable.playerId eq stats.playerId.value }) {
            it[mobsKilled] = stats.mobsKilled
            it[playersKilled] = stats.playersKilled
            it[deaths] = stats.deaths
            it[dungeonsCompleted] = stats.dungeonsCompleted
            it[questsCompleted] = stats.questsCompleted
            it[achievementsUnlocked] = stats.achievementsUnlocked
            it[distanceTraveled] = stats.distanceTraveled
            it[blocksMined] = stats.blocksMined
            it[blocksPlaced] = stats.blocksPlaced
        }

        if (updated == 0) {
            return@dbQuery Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家统计不存在")
        }

        Result.success(
            PlayerStatsTable.selectAll().where { PlayerStatsTable.playerId eq stats.playerId.value }
                .single().toPlayerStats()
        )
    }

    override suspend fun updateLastLogin(playerId: PlayerId): Unit = db.dbQuery {
        val now = System.currentTimeMillis()
        Players.update({ Players.playerId eq playerId.value }) {
            it[lastLoginAt] = now
            it[updatedAt] = now
        }
    }

    override suspend fun addOnlineTime(playerId: PlayerId, seconds: Long): Unit = db.dbQuery {
        Players.update({ Players.playerId eq playerId.value }) {
            it.update(totalOnlineTime, totalOnlineTime + seconds)
            it[updatedAt] = System.currentTimeMillis()
        }
    }

    override suspend fun search(keyword: String, limit: Int): List<PlayerEntity> = db.dbQuery {
        val pattern = "%${keyword}%"
        Players.selectAll().where {
            (Players.username like pattern) or (Players.displayName like pattern)
        }.limit(limit).map { it.toPlayerEntity() }
    }

    override suspend fun getLeaderboard(type: LeaderboardType, limit: Int): List<PlayerEntity> = db.dbQuery {
        when (type) {
            LeaderboardType.LEVEL -> Players.selectAll()
                .orderBy(Players.level, SortOrder.DESC)
                .limit(limit)
                .map { it.toPlayerEntity() }

            LeaderboardType.GOLD -> Players.selectAll()
                .orderBy(Players.gold, SortOrder.DESC)
                .limit(limit)
                .map { it.toPlayerEntity() }

            LeaderboardType.PVP_KILLS -> {
                Players.innerJoin(PlayerStatsTable)
                    .selectAll()
                    .orderBy(PlayerStatsTable.playersKilled, SortOrder.DESC)
                    .limit(limit)
                    .map { it.toPlayerEntity() }
            }

            LeaderboardType.DUNGEONS_COMPLETED -> {
                Players.innerJoin(PlayerStatsTable)
                    .selectAll()
                    .orderBy(PlayerStatsTable.dungeonsCompleted, SortOrder.DESC)
                    .limit(limit)
                    .map { it.toPlayerEntity() }
            }

            LeaderboardType.ACHIEVEMENTS -> {
                Players.innerJoin(PlayerStatsTable)
                    .selectAll()
                    .orderBy(PlayerStatsTable.achievementsUnlocked, SortOrder.DESC)
                    .limit(limit)
                    .map { it.toPlayerEntity() }
            }
        }
    }

    private fun findByIdInternal(playerId: PlayerId): PlayerEntity? =
        Players.selectAll().where { Players.playerId eq playerId.value }
            .singleOrNull()?.toPlayerEntity()
}

// ─── ResultRow 转换 ──────────────────────────────────────

private fun ResultRow.toPlayerEntity(): SimplePlayerEntity = SimplePlayerEntity(
    playerId = PlayerId(this[Players.playerId]),
    username = this[Players.username],
    displayName = this[Players.displayName],
    level = this[Players.level],
    experience = this[Players.experience],
    gold = this[Players.gold],
    diamond = this[Players.diamond],
    vipLevel = this[Players.vipLevel],
    firstLoginAt = this[Players.firstLoginAt],
    lastLoginAt = this[Players.lastLoginAt],
    totalOnlineTime = this[Players.totalOnlineTime],
    isBanned = this[Players.isBanned],
    banReason = this[Players.banReason],
    banExpireAt = this[Players.banExpireAt],
    createdAt = this[Players.createdAt],
    updatedAt = this[Players.updatedAt]
)

private fun ResultRow.toPlayerStats(): SimplePlayerStats = SimplePlayerStats(
    playerId = PlayerId(this[PlayerStatsTable.playerId]),
    mobsKilled = this[PlayerStatsTable.mobsKilled],
    playersKilled = this[PlayerStatsTable.playersKilled],
    deaths = this[PlayerStatsTable.deaths],
    dungeonsCompleted = this[PlayerStatsTable.dungeonsCompleted],
    questsCompleted = this[PlayerStatsTable.questsCompleted],
    achievementsUnlocked = this[PlayerStatsTable.achievementsUnlocked],
    distanceTraveled = this[PlayerStatsTable.distanceTraveled],
    blocksMined = this[PlayerStatsTable.blocksMined],
    blocksPlaced = this[PlayerStatsTable.blocksPlaced]
)
