package com.azathoth.game.engine.player

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.world.WorldPosition

/**
 * 游戏模式
 */
enum class GameMode {
    SURVIVAL,
    CREATIVE,
    ADVENTURE,
    SPECTATOR
}

/**
 * 游戏玩家接口
 */
interface GamePlayer : LivingEntity {
    /** 玩家ID */
    val playerId: PlayerId
    
    /** 玩家名称 */
    val name: String
    
    /** 显示名称 */
    var displayName: String
    
    /** 游戏模式 */
    var gameMode: GameMode
    
    /** 经验等级 */
    var level: Int
    
    /** 经验值 */
    var experience: Float
    
    /** 飞行状态 */
    var isFlying: Boolean
    
    /** 是否允许飞行 */
    var allowFlight: Boolean
    
    /** 飞行速度 */
    var flySpeed: Float
    
    /** 行走速度 */
    var walkSpeed: Float
    
    /** 是否隐身 */
    var isInvisible: Boolean
    
    /** 饥饿值 */
    var foodLevel: Int
    
    /** 饱和度 */
    var saturation: Float
    
    /** 背包 */
    val inventory: PlayerInventory
    
    /** 末影箱 */
    val enderChest: Inventory
    
    /** 是否在线 */
    val isOnline: Boolean
    
    /** 延迟（毫秒） */
    val ping: Int
    
    /** 发送消息 */
    suspend fun sendMessage(message: String)
    
    /** 发送动作栏消息 */
    suspend fun sendActionBar(message: String)
    
    /** 发送标题 */
    suspend fun sendTitle(title: String, subtitle: String, fadeIn: Int, stay: Int, fadeOut: Int)
    
    /** 播放音效 */
    suspend fun playSound(sound: String, volume: Float, pitch: Float)
    
    /** 发送粒子效果 */
    suspend fun spawnParticle(particle: String, position: WorldPosition, count: Int)
    
    /** 踢出玩家 */
    suspend fun kick(reason: String)
    
    /** 设置资源包 */
    suspend fun setResourcePack(url: String, hash: String, required: Boolean, prompt: String?)
    
    /** 重生 */
    suspend fun respawn()
    
    /** 更新玩家列表名称 */
    suspend fun setPlayerListName(name: String)
    
    /** 隐藏玩家 */
    suspend fun hidePlayer(player: GamePlayer)
    
    /** 显示玩家 */
    suspend fun showPlayer(player: GamePlayer)
    
    /** 是否能看到指定玩家 */
    fun canSee(player: GamePlayer): Boolean
}

/**
 * 背包接口
 */
interface Inventory {
    /** 背包大小 */
    val size: Int
    
    /** 获取物品 */
    fun getItem(slot: Int): ItemStack?
    
    /** 设置物品 */
    fun setItem(slot: Int, item: ItemStack?)
    
    /** 添加物品 */
    fun addItem(vararg items: ItemStack): Map<Int, ItemStack>
    
    /** 移除物品 */
    fun removeItem(vararg items: ItemStack): Map<Int, ItemStack>
    
    /** 清空背包 */
    fun clear()
    
    /** 是否包含指定物品 */
    fun contains(item: ItemStack): Boolean
    
    /** 获取所有内容 */
    fun getContents(): Array<ItemStack?>
    
    /** 设置所有内容 */
    fun setContents(items: Array<ItemStack?>)
}

/**
 * 玩家背包
 */
interface PlayerInventory : Inventory {
    /** 主手物品 */
    var itemInMainHand: ItemStack?
    
    /** 副手物品 */
    var itemInOffHand: ItemStack?
    
    /** 头盔 */
    var helmet: ItemStack?
    
    /** 胸甲 */
    var chestplate: ItemStack?
    
    /** 护腿 */
    var leggings: ItemStack?
    
    /** 靴子 */
    var boots: ItemStack?
    
    /** 当前选中的槽位 */
    var heldItemSlot: Int
}

/**
 * 物品栈
 */
interface ItemStack {
    /** 物品类型 */
    val type: String
    
    /** 数量 */
    var amount: Int
    
    /** 物品元数据 */
    val meta: ItemMeta?
    
    /** 克隆物品 */
    fun clone(): ItemStack
    
    /** 是否相似（忽略数量） */
    fun isSimilar(other: ItemStack): Boolean
}

/**
 * 物品元数据
 */
interface ItemMeta {
    /** 显示名称 */
    var displayName: String?
    
    /** 描述 */
    var lore: List<String>?
    
    /** 自定义模型数据 */
    var customModelData: Int?
    
    /** 是否不可破坏 */
    var isUnbreakable: Boolean
    
    /** 克隆元数据 */
    fun clone(): ItemMeta
}
