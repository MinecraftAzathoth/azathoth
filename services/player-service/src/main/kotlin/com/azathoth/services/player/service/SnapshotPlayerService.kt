package com.azathoth.services.player.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.core.common.snapshot.SnapshotRecord
import com.azathoth.core.common.snapshot.SnapshotStore
import com.azathoth.services.player.repository.PlayerEntity
import com.azathoth.services.player.repository.PlayerStats
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private val logger = KotlinLogging.logger {}

private val snapshotJson = Json { prettyPrint = false; encodeDefaults = true }

/**
 * 玩家实体的可序列化快照
 */
@Serializable
private data class PlayerSnapshot(
    val playerId: String,
    val username: String,
    val displayName: String,
    val level: Int,
    val experience: Long,
    val gold: Long,
    val diamond: Long,
    val vipLevel: Int,
    val lastLoginAt: Long,
    val totalOnlineTime: Long,
    val isBanned: Boolean,
    val banReason: String? = null,
    val banExpireAt: Long? = null,
    val updatedAt: Long
)

private fun PlayerEntity.toSnapshot() = PlayerSnapshot(
    playerId = playerId.value,
    username = username,
    displayName = displayName,
    level = level,
    experience = experience,
    gold = gold,
    diamond = diamond,
    vipLevel = vipLevel,
    lastLoginAt = lastLoginAt,
    totalOnlineTime = totalOnlineTime,
    isBanned = isBanned,
    banReason = banReason,
    banExpireAt = banExpireAt,
    updatedAt = updatedAt
)

/**
 * 快照装饰器 — 包装 PlayerService，在写操作前后自动记录快照到 SnapshotStore。
 *
 * 快照写入在独立协程中异步执行，不阻塞业务主流程。
 */
class SnapshotPlayerService(
    private val delegate: PlayerService,
    private val snapshotStore: SnapshotStore
) : PlayerService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ─── 读操作：直接委托 ──────────────────────────────────

    override suspend fun getPlayer(playerId: PlayerId) = delegate.getPlayer(playerId)
    override suspend fun isBanned(playerId: PlayerId) = delegate.isBanned(playerId)
    override suspend fun getStats(playerId: PlayerId) = delegate.getStats(playerId)
    override suspend fun onPlayerJoin(playerId: PlayerId) = delegate.onPlayerJoin(playerId)
    override suspend fun onPlayerLeave(playerId: PlayerId) = delegate.onPlayerLeave(playerId)

    // ─── 写操作：前后快照 ──────────────────────────────────

    override suspend fun getOrCreate(playerId: PlayerId, username: String): Result<PlayerEntity> {
        val result = delegate.getOrCreate(playerId, username)
        if (result.isSuccess) {
            val after = result.getOrNull()
            if (after != null) {
                fireSnapshot(playerId.value, "getOrCreate", "{}", snapshotJson.encodeToString(after.toSnapshot()))
            }
        }
        return result
    }

    override suspend fun updatePlayer(player: PlayerEntity): Result<PlayerEntity> {
        val before = delegate.getPlayer(player.playerId).getOrNull()
        val result = delegate.updatePlayer(player)
        if (result.isSuccess && before != null) {
            val after = result.getOrNull()
            if (after != null) {
                fireSnapshot(
                    player.playerId.value, "updatePlayer",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun addExperience(playerId: PlayerId, amount: Long): Result<LevelUpResult> {
        val before = delegate.getPlayer(playerId).getOrNull()
        val result = delegate.addExperience(playerId, amount)
        if (result.isSuccess && before != null) {
            val after = delegate.getPlayer(playerId).getOrNull()
            if (after != null) {
                fireSnapshot(
                    playerId.value, "addExperience",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun addGold(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        val before = delegate.getPlayer(playerId).getOrNull()
        val result = delegate.addGold(playerId, amount, reason)
        if (result.isSuccess && before != null) {
            val after = delegate.getPlayer(playerId).getOrNull()
            if (after != null) {
                fireSnapshot(
                    playerId.value, "addGold",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun deductGold(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        val before = delegate.getPlayer(playerId).getOrNull()
        val result = delegate.deductGold(playerId, amount, reason)
        if (result.isSuccess && before != null) {
            val after = delegate.getPlayer(playerId).getOrNull()
            if (after != null) {
                fireSnapshot(
                    playerId.value, "deductGold",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun addDiamond(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        val before = delegate.getPlayer(playerId).getOrNull()
        val result = delegate.addDiamond(playerId, amount, reason)
        if (result.isSuccess && before != null) {
            val after = delegate.getPlayer(playerId).getOrNull()
            if (after != null) {
                fireSnapshot(
                    playerId.value, "addDiamond",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun deductDiamond(playerId: PlayerId, amount: Long, reason: String): Result<Long> {
        val before = delegate.getPlayer(playerId).getOrNull()
        val result = delegate.deductDiamond(playerId, amount, reason)
        if (result.isSuccess && before != null) {
            val after = delegate.getPlayer(playerId).getOrNull()
            if (after != null) {
                fireSnapshot(
                    playerId.value, "deductDiamond",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun banPlayer(playerId: PlayerId, reason: String, durationSeconds: Long?): Result<Unit> {
        val before = delegate.getPlayer(playerId).getOrNull()
        val result = delegate.banPlayer(playerId, reason, durationSeconds)
        if (result.isSuccess && before != null) {
            val after = delegate.getPlayer(playerId).getOrNull()
            if (after != null) {
                fireSnapshot(
                    playerId.value, "banPlayer",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun unbanPlayer(playerId: PlayerId): Result<Unit> {
        val before = delegate.getPlayer(playerId).getOrNull()
        val result = delegate.unbanPlayer(playerId)
        if (result.isSuccess && before != null) {
            val after = delegate.getPlayer(playerId).getOrNull()
            if (after != null) {
                fireSnapshot(
                    playerId.value, "unbanPlayer",
                    snapshotJson.encodeToString(before.toSnapshot()),
                    snapshotJson.encodeToString(after.toSnapshot())
                )
            }
        }
        return result
    }

    override suspend fun updateStats(playerId: PlayerId, updater: (PlayerStats) -> Unit): Result<PlayerStats> {
        // Stats 快照暂不记录，直接委托
        return delegate.updateStats(playerId, updater)
    }

    // ─── 内部：异步写快照 ─────────────────────────────────

    private fun fireSnapshot(playerId: String, operation: String, beforeJson: String, afterJson: String) {
        scope.launch {
            try {
                snapshotStore.save(
                    SnapshotRecord(
                        snapshotId = UUID.randomUUID().toString(),
                        playerId = playerId,
                        entityType = "player",
                        operation = operation,
                        beforeJson = beforeJson,
                        afterJson = afterJson,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "快照写入失败: player=$playerId op=$operation" }
            }
        }
    }
}
