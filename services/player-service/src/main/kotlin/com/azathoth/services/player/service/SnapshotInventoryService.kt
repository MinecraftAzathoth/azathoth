package com.azathoth.services.player.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.core.common.snapshot.SnapshotRecord
import com.azathoth.core.common.snapshot.SnapshotStore
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

private val inventoryJson = Json { prettyPrint = false; encodeDefaults = true }

@Serializable
private data class InventorySnapshot(
    val playerId: String,
    val items: List<ItemSnapshot>
)

@Serializable
private data class ItemSnapshot(
    val slot: Int,
    val itemId: String,
    val amount: Int
)

private suspend fun InventoryService.snapshot(playerId: PlayerId): String {
    val inv = getInventory(playerId).getOrNull() ?: return "{}"
    return inventoryJson.encodeToString(
        InventorySnapshot(
            playerId = playerId.value,
            items = inv.items.map { ItemSnapshot(it.slot, it.itemId, it.amount) }
        )
    )
}

/**
 * 快照装饰器 — 包装 InventoryService，在写操作前后自动记录快照。
 */
class SnapshotInventoryService(
    private val delegate: InventoryService,
    private val snapshotStore: SnapshotStore
) : InventoryService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ─── 读操作：直接委托 ──────────────────────────────────

    override suspend fun getInventory(playerId: PlayerId) = delegate.getInventory(playerId)
    override suspend fun hasItem(playerId: PlayerId, itemId: String, amount: Int) = delegate.hasItem(playerId, itemId, amount)
    override suspend fun getItemCount(playerId: PlayerId, itemId: String) = delegate.getItemCount(playerId, itemId)

    // ─── 写操作：前后快照 ──────────────────────────────────

    override suspend fun addItem(playerId: PlayerId, itemId: String, amount: Int): Result<Unit> {
        val before = delegate.snapshot(playerId)
        val result = delegate.addItem(playerId, itemId, amount)
        if (result.isSuccess) {
            val after = delegate.snapshot(playerId)
            fireSnapshot(playerId.value, "addItem", before, after)
        }
        return result
    }

    override suspend fun removeItem(playerId: PlayerId, itemId: String, amount: Int): Result<Unit> {
        val before = delegate.snapshot(playerId)
        val result = delegate.removeItem(playerId, itemId, amount)
        if (result.isSuccess) {
            val after = delegate.snapshot(playerId)
            fireSnapshot(playerId.value, "removeItem", before, after)
        }
        return result
    }

    override suspend fun clearInventory(playerId: PlayerId): Result<Unit> {
        val before = delegate.snapshot(playerId)
        val result = delegate.clearInventory(playerId)
        if (result.isSuccess) {
            val after = delegate.snapshot(playerId)
            fireSnapshot(playerId.value, "clearInventory", before, after)
        }
        return result
    }

    // ─── 内部：异步写快照 ─────────────────────────────────

    private fun fireSnapshot(playerId: String, operation: String, beforeJson: String, afterJson: String) {
        scope.launch {
            try {
                snapshotStore.save(
                    SnapshotRecord(
                        snapshotId = UUID.randomUUID().toString(),
                        playerId = playerId,
                        entityType = "inventory",
                        operation = operation,
                        beforeJson = beforeJson,
                        afterJson = afterJson,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "背包快照写入失败: player=$playerId op=$operation" }
            }
        }
    }
}
