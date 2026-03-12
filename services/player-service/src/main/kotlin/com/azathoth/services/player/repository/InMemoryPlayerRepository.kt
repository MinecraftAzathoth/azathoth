package com.azathoth.services.player.repository

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

data class SimplePlayerEntity(
    override val playerId: PlayerId,
    override val username: String,
    override var displayName: String,
    override var level: Int = 1,
    override var experience: Long = 0,
    override var gold: Long = 0,
    override var diamond: Long = 0,
    override var vipLevel: Int = 0,
    override val firstLoginAt: Long = System.currentTimeMillis(),
    override var lastLoginAt: Long = System.currentTimeMillis(),
    override var totalOnlineTime: Long = 0,
    override var isBanned: Boolean = false,
    override var banReason: String? = null,
    override var banExpireAt: Long? = null,
    override val createdAt: Long = System.currentTimeMillis(),
    override var updatedAt: Long = System.currentTimeMillis()
) : PlayerEntity

data class SimplePlayerStats(
    override val playerId: PlayerId,
    override var mobsKilled: Long = 0,
    override var playersKilled: Long = 0,
    override var deaths: Long = 0,
    override var dungeonsCompleted: Long = 0,
    override var questsCompleted: Long = 0,
    override var achievementsUnlocked: Int = 0,
    override var distanceTraveled: Long = 0,
    override var blocksMined: Long = 0,
    override var blocksPlaced: Long = 0
) : PlayerStats

class InMemoryPlayerRepository : PlayerRepository {

    private val players = ConcurrentHashMap<String, SimplePlayerEntity>()
    private val stats = ConcurrentHashMap<String, SimplePlayerStats>()

    override suspend fun create(playerId: PlayerId, username: String): Result<PlayerEntity> {
        if (players.containsKey(playerId.value)) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "玩家已存在: ${playerId.value}")
        }
        if (players.values.any { it.username == username }) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "用户名已存在: $username")
        }
        val entity = SimplePlayerEntity(playerId = playerId, username = username, displayName = username)
        players[playerId.value] = entity
        stats[playerId.value] = SimplePlayerStats(playerId = playerId)
        logger.info { "创建玩家: $username (${playerId.value})" }
        return Result.success(entity)
    }

    override suspend fun findById(playerId: PlayerId): PlayerEntity? = players[playerId.value]

    override suspend fun findByUsername(username: String): PlayerEntity? =
        players.values.firstOrNull { it.username == username }

    override suspend fun save(player: PlayerEntity): Result<PlayerEntity> {
        val entity = player as? SimplePlayerEntity
            ?: return Result.failure(ErrorCodes.INVALID_ARGUMENT, "不支持的实体类型")
        entity.updatedAt = System.currentTimeMillis()
        players[player.playerId.value] = entity
        return Result.success(entity)
    }

    override suspend fun delete(playerId: PlayerId): Result<Unit> {
        players.remove(playerId.value)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在: ${playerId.value}")
        stats.remove(playerId.value)
        return Result.success(Unit)
    }

    override suspend fun exists(playerId: PlayerId): Boolean = players.containsKey(playerId.value)

    override suspend fun existsByUsername(username: String): Boolean =
        players.values.any { it.username == username }

    override suspend fun getStats(playerId: PlayerId): PlayerStats? = stats[playerId.value]

    override suspend fun saveStats(stats: PlayerStats): Result<PlayerStats> {
        val simple = stats as? SimplePlayerStats
            ?: return Result.failure(ErrorCodes.INVALID_ARGUMENT, "不支持的统计类型")
        this.stats[stats.playerId.value] = simple
        return Result.success(simple)
    }

    override suspend fun updateLastLogin(playerId: PlayerId) {
        players.computeIfPresent(playerId.value) { _, entity ->
            entity.copy(lastLoginAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
        }
    }

    override suspend fun addOnlineTime(playerId: PlayerId, seconds: Long) {
        players.computeIfPresent(playerId.value) { _, entity ->
            entity.copy(
                totalOnlineTime = entity.totalOnlineTime + seconds,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun search(keyword: String, limit: Int): List<PlayerEntity> =
        players.values
            .filter { it.username.contains(keyword, ignoreCase = true) || it.displayName.contains(keyword, ignoreCase = true) }
            .take(limit)

    override suspend fun getLeaderboard(type: LeaderboardType, limit: Int): List<PlayerEntity> {
        val comparator: Comparator<SimplePlayerEntity> = when (type) {
            LeaderboardType.LEVEL -> compareByDescending { it.level }
            LeaderboardType.GOLD -> compareByDescending { it.gold }
            LeaderboardType.PVP_KILLS -> compareByDescending { stats[it.playerId.value]?.playersKilled ?: 0 }
            LeaderboardType.DUNGEONS_COMPLETED -> compareByDescending { stats[it.playerId.value]?.dungeonsCompleted ?: 0 }
            LeaderboardType.ACHIEVEMENTS -> compareByDescending { stats[it.playerId.value]?.achievementsUnlocked ?: 0 }
        }
        return players.values.sortedWith(comparator).take(limit)
    }
}
