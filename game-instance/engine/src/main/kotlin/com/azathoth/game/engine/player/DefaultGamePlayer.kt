package com.azathoth.game.engine.player

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.game.engine.entity.DefaultLivingEntity
import com.azathoth.game.engine.entity.EntityType
import com.azathoth.game.engine.world.World
import com.azathoth.game.engine.world.WorldPosition
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val logger = KotlinLogging.logger {}

/**
 * 简单物品元数据实现
 */
data class SimpleItemMeta(
    override var displayName: String? = null,
    override var lore: List<String>? = null,
    override var customModelData: Int? = null,
    override var isUnbreakable: Boolean = false
) : ItemMeta {
    override fun clone(): ItemMeta = copy(lore = lore?.toList())
}

/**
 * 简单物品栈实现
 */
data class SimpleItemStack(
    override val type: String,
    override var amount: Int = 1,
    override val meta: ItemMeta? = null
) : ItemStack {
    override fun clone(): ItemStack = SimpleItemStack(type, amount, meta?.clone())
    override fun isSimilar(other: ItemStack): Boolean = type == other.type && meta == other.meta
}

/**
 * 默认背包实现
 */
open class DefaultInventory(override val size: Int) : Inventory {
    protected val items = arrayOfNulls<ItemStack>(size)

    override fun getItem(slot: Int): ItemStack? {
        if (slot < 0 || slot >= size) return null
        return items[slot]
    }

    override fun setItem(slot: Int, item: ItemStack?) {
        if (slot < 0 || slot >= size) return
        items[slot] = item
    }

    override fun addItem(vararg items: ItemStack): Map<Int, ItemStack> {
        val overflow = mutableMapOf<Int, ItemStack>()
        items.forEachIndexed { index, item ->
            var placed = false
            for (i in 0 until size) {
                if (this.items[i] == null) {
                    this.items[i] = item.clone()
                    placed = true
                    break
                }
            }
            if (!placed) overflow[index] = item
        }
        return overflow
    }

    override fun removeItem(vararg items: ItemStack): Map<Int, ItemStack> {
        val notRemoved = mutableMapOf<Int, ItemStack>()
        items.forEachIndexed { index, item ->
            var removed = false
            for (i in 0 until size) {
                val slot = this.items[i]
                if (slot != null && slot.isSimilar(item) && slot.amount >= item.amount) {
                    slot.amount -= item.amount
                    if (slot.amount <= 0) this.items[i] = null
                    removed = true
                    break
                }
            }
            if (!removed) notRemoved[index] = item
        }
        return notRemoved
    }

    override fun clear() {
        items.fill(null)
    }

    override fun contains(item: ItemStack): Boolean =
        items.any { it != null && it.isSimilar(item) && it.amount >= item.amount }

    override fun getContents(): Array<ItemStack?> = items.copyOf()

    override fun setContents(items: Array<ItemStack?>) {
        for (i in items.indices.take(size)) {
            this.items[i] = items[i]
        }
    }
}

/**
 * 默认玩家背包实现
 */
class DefaultPlayerInventory : DefaultInventory(36), PlayerInventory {
    // 装备槽: 36=主手, 37=副手, 38=头盔, 39=胸甲, 40=护腿, 41=靴子
    override var itemInMainHand: ItemStack?
        get() = items.getOrNull(heldItemSlot)
        set(value) { if (heldItemSlot in 0 until size) items[heldItemSlot] = value }

    override var itemInOffHand: ItemStack? = null
    override var helmet: ItemStack? = null
    override var chestplate: ItemStack? = null
    override var leggings: ItemStack? = null
    override var boots: ItemStack? = null
    override var heldItemSlot: Int = 0
}

/**
 * 默认游戏玩家实现
 */
class DefaultGamePlayer(
    override val playerId: PlayerId,
    override val name: String,
    world: World,
    uuid: UUID = UUID.randomUUID()
) : DefaultLivingEntity(EntityType.PLAYER, world, uuid), GamePlayer {

    override var displayName: String = name
    override var gameMode: GameMode = GameMode.SURVIVAL
    override var level: Int = 0
    override var experience: Float = 0f
    override var isFlying: Boolean = false
    override var allowFlight: Boolean = false
    override var flySpeed: Float = 0.1f
    override var walkSpeed: Float = 0.2f
    override var isInvisible: Boolean = false
    override var foodLevel: Int = 20
    override var saturation: Float = 5.0f

    override val inventory: PlayerInventory = DefaultPlayerInventory()
    override val enderChest: Inventory = DefaultInventory(27)

    @Volatile
    override var isOnline: Boolean = true
    override val ping: Int get() = 0

    private val hiddenPlayers = ConcurrentHashMap.newKeySet<PlayerId>()
    private val messages = CopyOnWriteArrayList<String>()

    override suspend fun sendMessage(message: String) {
        messages.add(message)
        logger.debug { "[$name] 消息: $message" }
    }

    override suspend fun sendActionBar(message: String) {
        logger.debug { "[$name] 动作栏: $message" }
    }

    override suspend fun sendTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int) {
        logger.debug { "[$name] 标题: $title / $subtitle" }
    }

    override suspend fun playSound(sound: String, volume: Float, pitch: Float) {
        logger.debug { "[$name] 音效: $sound" }
    }

    override suspend fun spawnParticle(particle: String, position: WorldPosition, count: Int) {
        logger.debug { "[$name] 粒子: $particle x$count" }
    }

    override suspend fun kick(reason: String) {
        isOnline = false
        logger.info { "踢出玩家 $name: $reason" }
    }

    override suspend fun setResourcePack(url: String, hash: String, required: Boolean, prompt: String?) {
        logger.debug { "[$name] 设置资源包: $url" }
    }

    override suspend fun respawn() {
        health = maxHealth
        position = world.spawnPosition
        logger.debug { "[$name] 重生" }
    }

    override suspend fun setPlayerListName(name: String) {
        displayName = name
    }

    override suspend fun hidePlayer(player: GamePlayer) {
        hiddenPlayers.add(player.playerId)
    }

    override suspend fun showPlayer(player: GamePlayer) {
        hiddenPlayers.remove(player.playerId)
    }

    override fun canSee(player: GamePlayer): Boolean = !hiddenPlayers.contains(player.playerId)

    /** 获取收到的消息（测试用） */
    fun getReceivedMessages(): List<String> = messages.toList()
}
