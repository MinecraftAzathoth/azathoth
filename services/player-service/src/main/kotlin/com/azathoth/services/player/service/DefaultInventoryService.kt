package com.azathoth.services.player.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

data class SimpleInventoryItem(
    override val slot: Int,
    override val itemId: String,
    override val amount: Int,
    override val data: Map<String, Any> = emptyMap()
) : InventoryItem

data class SimpleInventoryData(
    override val playerId: PlayerId,
    override val items: List<InventoryItem>,
    override val capacity: Int = 36,
    override val usedSlots: Int = items.size
) : InventoryData

class DefaultInventoryService : InventoryService {

    /** playerId -> (itemId -> SimpleInventoryItem) */
    private val inventories = ConcurrentHashMap<String, ConcurrentHashMap<String, SimpleInventoryItem>>()

    private fun getOrCreateBag(playerId: PlayerId): ConcurrentHashMap<String, SimpleInventoryItem> =
        inventories.getOrPut(playerId.value) { ConcurrentHashMap() }

    override suspend fun getInventory(playerId: PlayerId): Result<InventoryData> {
        val bag = getOrCreateBag(playerId)
        val items = bag.values.sortedBy { it.slot }
        return Result.success(SimpleInventoryData(playerId = playerId, items = items))
    }

    override suspend fun addItem(playerId: PlayerId, itemId: String, amount: Int): Result<Unit> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "数量必须为正数")
        val bag = getOrCreateBag(playerId)
        bag.compute(itemId) { _, existing ->
            if (existing != null) {
                existing.copy(amount = existing.amount + amount)
            } else {
                SimpleInventoryItem(slot = bag.size, itemId = itemId, amount = amount)
            }
        }
        logger.debug { "玩家 ${playerId.value} 添加物品 $itemId x$amount" }
        return Result.success(Unit)
    }

    override suspend fun removeItem(playerId: PlayerId, itemId: String, amount: Int): Result<Unit> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "数量必须为正数")
        val bag = getOrCreateBag(playerId)
        val existing = bag[itemId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "物品不存在: $itemId")
        if (existing.amount < amount) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "物品数量不足")
        }
        if (existing.amount == amount) {
            bag.remove(itemId)
        } else {
            bag[itemId] = existing.copy(amount = existing.amount - amount)
        }
        logger.debug { "玩家 ${playerId.value} 移除物品 $itemId x$amount" }
        return Result.success(Unit)
    }

    override suspend fun hasItem(playerId: PlayerId, itemId: String, amount: Int): Boolean {
        val bag = inventories[playerId.value] ?: return false
        return (bag[itemId]?.amount ?: 0) >= amount
    }

    override suspend fun getItemCount(playerId: PlayerId, itemId: String): Int {
        val bag = inventories[playerId.value] ?: return 0
        return bag[itemId]?.amount ?: 0
    }

    override suspend fun clearInventory(playerId: PlayerId): Result<Unit> {
        inventories.remove(playerId.value)
        logger.info { "清空玩家 ${playerId.value} 背包" }
        return Result.success(Unit)
    }
}
