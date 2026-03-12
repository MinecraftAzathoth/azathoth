package com.azathoth.services.player.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.player.repository.PlayerEntity
import com.azathoth.services.player.repository.PlayerRepository
import com.azathoth.services.player.repository.PlayerStats
import com.azathoth.services.player.repository.SimplePlayerStats
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sqrt

private val logger = KotlinLogging.logger {}

data class SimpleLevelUpResult(
    override val leveledUp: Boolean,
    override val newLevel: Int,
    override val currentExperience: Long,
    override val experienceToNextLevel: Long,
    override val levelsGained: Int
) : LevelUpResult

class DefaultPlayerService(
    private val repository: PlayerRepository
) : PlayerService {

    /** 记录玩家上线时间，用于计算在线时长 */
    private val onlineSince = ConcurrentHashMap<String, Long>()

    override suspend fun getOrCreate(playerId: PlayerId, username: String): Result<PlayerEntity> {
        val existing = repository.findById(playerId)
        if (existing != null) return Result.success(existing)
        return repository.create(playerId, username)
    }

    override suspend fun getPlayer(playerId: PlayerId): Result<PlayerEntity> {
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在: ${playerId.value}")
        return Result.success(player)
    }

    override suspend fun updatePlayer(player: PlayerEntity): Result<PlayerEntity> =
        repository.save(player)

    override suspend fun onPlayerJoin(playerId: PlayerId) {
        repository.updateLastLogin(playerId)
        onlineSince[playerId.value] = System.currentTimeMillis()
        logger.info { "玩家上线: ${playerId.value}" }
    }

    override suspend fun onPlayerLeave(playerId: PlayerId) {
        val joinTime = onlineSince.remove(playerId.value)
        if (joinTime != null) {
            val seconds = (System.currentTimeMillis() - joinTime) / 1000
            repository.addOnlineTime(playerId, seconds)
        }
        logger.info { "玩家下线: ${playerId.value}" }
    }

    override suspend fun addExperience(playerId: PlayerId, amount: Long): Result<LevelUpResult> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "经验值必须为正数")
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在")

        val oldLevel = player.level
        player.experience += amount
        val newLevel = calculateLevel(player.experience)
        player.level = newLevel
        repository.save(player)

        val levelsGained = newLevel - oldLevel
        if (levelsGained > 0) {
            logger.info { "玩家 ${playerId.value} 升级: $oldLevel -> $newLevel" }
        }

        return Result.success(
            SimpleLevelUpResult(
                leveledUp = levelsGained > 0,
                newLevel = newLevel,
                currentExperience = player.experience,
                experienceToNextLevel = experienceForLevel(newLevel + 1) - player.experience,
                levelsGained = levelsGained
            )
        )
    }

    override suspend fun addGold(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "金币数量必须为正数")
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在")
        player.gold += amount
        repository.save(player)
        logger.debug { "玩家 ${playerId.value} 增加金币 $amount, 原因: $reason" }
        return Result.success(player.gold)
    }

    override suspend fun deductGold(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "金币数量必须为正数")
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在")
        if (player.gold < amount) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "金币不足")
        player.gold -= amount
        repository.save(player)
        logger.debug { "玩家 ${playerId.value} 扣除金币 $amount, 原因: $reason" }
        return Result.success(player.gold)
    }

    override suspend fun addDiamond(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "钻石数量必须为正数")
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在")
        player.diamond += amount
        repository.save(player)
        logger.debug { "玩家 ${playerId.value} 增加钻石 $amount, 原因: $reason" }
        return Result.success(player.diamond)
    }

    override suspend fun deductDiamond(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "钻石数量必须为正数")
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在")
        if (player.diamond < amount) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "钻石不足")
        player.diamond -= amount
        repository.save(player)
        logger.debug { "玩家 ${playerId.value} 扣除钻石 $amount, 原因: $reason" }
        return Result.success(player.diamond)
    }

    override suspend fun banPlayer(playerId: PlayerId, reason: String, durationSeconds: Long?): Result<Unit> {
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在")
        player.isBanned = true
        player.banReason = reason
        player.banExpireAt = durationSeconds?.let { System.currentTimeMillis() + it * 1000 }
        repository.save(player)
        logger.info { "封禁玩家 ${playerId.value}, 原因: $reason" }
        return Result.success(Unit)
    }

    override suspend fun unbanPlayer(playerId: PlayerId): Result<Unit> {
        val player = repository.findById(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家不存在")
        player.isBanned = false
        player.banReason = null
        player.banExpireAt = null
        repository.save(player)
        logger.info { "解封玩家 ${playerId.value}" }
        return Result.success(Unit)
    }

    override suspend fun isBanned(playerId: PlayerId): Boolean {
        val player = repository.findById(playerId) ?: return false
        if (!player.isBanned) return false
        val expireAt = player.banExpireAt
        if (expireAt != null && System.currentTimeMillis() > expireAt) {
            // 封禁已过期，自动解封
            player.isBanned = false
            player.banReason = null
            player.banExpireAt = null
            repository.save(player)
            return false
        }
        return true
    }

    override suspend fun getStats(playerId: PlayerId): Result<PlayerStats> {
        val s = repository.getStats(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家统计不存在")
        return Result.success(s)
    }

    override suspend fun updateStats(playerId: PlayerId, updater: (PlayerStats) -> Unit): Result<PlayerStats> {
        val s = repository.getStats(playerId)
            ?: return Result.failure(ErrorCodes.PLAYER_NOT_FOUND, "玩家统计不存在")
        updater(s)
        return repository.saveStats(s)
    }

    companion object {
        /** level = sqrt(experience / 100)，最低1级 */
        fun calculateLevel(experience: Long): Int =
            maxOf(1, sqrt(experience.toDouble() / 100.0).toInt())

        /** 达到指定等级所需的总经验 */
        fun experienceForLevel(level: Int): Long = level.toLong() * level.toLong() * 100
    }
}
