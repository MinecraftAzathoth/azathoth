package com.azathoth.game.engine.entity

import com.azathoth.game.engine.world.World
import com.azathoth.game.engine.world.WorldPosition
import java.util.UUID

/**
 * 实体类型
 */
enum class EntityType {
    PLAYER,
    MOB,
    NPC,
    ITEM,
    PROJECTILE,
    VEHICLE,
    OTHER
}

/**
 * 实体接口
 */
interface Entity {
    /** 实体ID */
    val entityId: Int
    
    /** 实体UUID */
    val uuid: UUID
    
    /** 实体类型 */
    val type: EntityType
    
    /** 实体名称 */
    var customName: String?
    
    /** 是否显示名称 */
    var customNameVisible: Boolean
    
    /** 所在世界 */
    val world: World
    
    /** 位置 */
    var position: WorldPosition
    
    /** 速度 */
    var velocity: Velocity
    
    /** 是否在地面上 */
    val isOnGround: Boolean
    
    /** 是否存活 */
    val isAlive: Boolean
    
    /** 是否已移除 */
    val isRemoved: Boolean
    
    /** 骑乘的实体 */
    val vehicle: Entity?
    
    /** 乘客列表 */
    val passengers: List<Entity>
    
    /** 实体元数据 */
    fun getMetadata(key: String): Any?
    
    /** 设置实体元数据 */
    fun setMetadata(key: String, value: Any?)
    
    /** 移除实体元数据 */
    fun removeMetadata(key: String)
    
    /** 传送实体 */
    suspend fun teleport(position: WorldPosition)
    
    /** 传送到另一个世界 */
    suspend fun teleport(world: World, position: WorldPosition)
    
    /** 添加乘客 */
    suspend fun addPassenger(entity: Entity)
    
    /** 移除乘客 */
    suspend fun removePassenger(entity: Entity)
    
    /** 移除实体 */
    suspend fun remove()
}

/**
 * 速度
 */
data class Velocity(val x: Double, val y: Double, val z: Double)

/**
 * 生物实体（有生命值）
 */
interface LivingEntity : Entity {
    /** 当前生命值 */
    var health: Double
    
    /** 最大生命值 */
    var maxHealth: Double
    
    /** 是否死亡 */
    val isDead: Boolean get() = health <= 0
    
    /** 最后受伤原因 */
    val lastDamageCause: DamageCause?
    
    /** 伤害实体 */
    suspend fun damage(amount: Double, source: DamageSource? = null)
    
    /** 治疗实体 */
    suspend fun heal(amount: Double)
    
    /** 杀死实体 */
    suspend fun kill()
    
    /** 添加药水效果 */
    suspend fun addPotionEffect(effect: PotionEffect)
    
    /** 移除药水效果 */
    suspend fun removePotionEffect(effectType: String)
    
    /** 获取药水效果 */
    fun getPotionEffects(): Collection<PotionEffect>
    
    /** 是否有指定药水效果 */
    fun hasPotionEffect(effectType: String): Boolean
}

/**
 * 伤害来源
 */
interface DamageSource {
    /** 伤害类型 */
    val type: DamageType
    
    /** 造成伤害的实体 */
    val damager: Entity?
    
    /** 伤害原因 */
    val cause: DamageCause
}

/**
 * 伤害类型
 */
enum class DamageType {
    PHYSICAL,
    MAGIC,
    FIRE,
    POISON,
    TRUE,
    FALL,
    VOID,
    EXPLOSION
}

/**
 * 伤害原因
 */
enum class DamageCause {
    ENTITY_ATTACK,
    ENTITY_SWEEP_ATTACK,
    PROJECTILE,
    FALL,
    FIRE,
    FIRE_TICK,
    LAVA,
    DROWNING,
    POISON,
    MAGIC,
    VOID,
    EXPLOSION,
    CUSTOM
}

/**
 * 药水效果
 */
interface PotionEffect {
    /** 效果类型 */
    val type: String
    
    /** 效果等级 */
    val amplifier: Int
    
    /** 持续时间（tick） */
    val duration: Int
    
    /** 是否显示粒子 */
    val particles: Boolean
    
    /** 是否显示图标 */
    val icon: Boolean
}

/**
 * 实体工厂
 */
interface EntityFactory {
    /** 创建实体 */
    fun <T : Entity> create(type: EntityType, entityClass: Class<T>): T
    
    /** 创建生物实体 */
    fun createLiving(type: String): LivingEntity
    
    /** 创建物品实体 */
    fun createItem(item: Any): Entity
}
