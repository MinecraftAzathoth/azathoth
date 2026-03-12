package com.azathoth.game.engine.entity

import com.azathoth.game.engine.world.World
import com.azathoth.game.engine.world.WorldPosition
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.CopyOnWriteArrayList

private val logger = KotlinLogging.logger {}

private val entityIdCounter = AtomicInteger(0)

/**
 * 简单药水效果实现
 */
data class SimplePotionEffect(
    override val type: String,
    override val amplifier: Int,
    override val duration: Int,
    override val particles: Boolean = true,
    override val icon: Boolean = true
) : PotionEffect

/**
 * 简单伤害来源实现
 */
data class SimpleDamageSource(
    override val type: DamageType,
    override val damager: Entity?,
    override val cause: DamageCause
) : DamageSource

/**
 * 默认实体实现
 */
open class DefaultEntity(
    override val type: EntityType,
    override val world: World,
    override val uuid: UUID = UUID.randomUUID()
) : Entity {
    override val entityId: Int = entityIdCounter.incrementAndGet()

    override var customName: String? = null
    override var customNameVisible: Boolean = false
    override var position: WorldPosition = WorldPosition(0.0, 0.0, 0.0)
    override var velocity: Velocity = Velocity(0.0, 0.0, 0.0)

    @Volatile
    override var isOnGround: Boolean = true

    private val _isAlive = AtomicBoolean(true)
    override val isAlive: Boolean get() = _isAlive.get()

    private val _isRemoved = AtomicBoolean(false)
    override val isRemoved: Boolean get() = _isRemoved.get()

    override var vehicle: Entity? = null
    private val _passengers = CopyOnWriteArrayList<Entity>()
    override val passengers: List<Entity> get() = _passengers.toList()

    private val metadata = ConcurrentHashMap<String, Any?>()

    override fun getMetadata(key: String): Any? = metadata[key]

    override fun setMetadata(key: String, value: Any?) {
        if (value != null) metadata[key] = value else metadata.remove(key)
    }

    override fun removeMetadata(key: String) {
        metadata.remove(key)
    }

    override suspend fun teleport(position: WorldPosition) {
        this.position = position
    }

    override suspend fun teleport(world: World, position: WorldPosition) {
        this.position = position
    }

    override suspend fun addPassenger(entity: Entity) {
        _passengers.add(entity)
        if (entity is DefaultEntity) {
            entity.vehicle = this
        }
    }

    override suspend fun removePassenger(entity: Entity) {
        _passengers.remove(entity)
        if (entity is DefaultEntity) {
            entity.vehicle = null
        }
    }

    override suspend fun remove() {
        _isAlive.set(false)
        _isRemoved.set(true)
        _passengers.forEach { (it as? DefaultEntity)?.vehicle = null }
        _passengers.clear()
        world.removeEntity(this)
        logger.debug { "移除实体 $entityId" }
    }
}

/**
 * 默认生物实体实现
 */
open class DefaultLivingEntity(
    type: EntityType,
    world: World,
    uuid: UUID = UUID.randomUUID()
) : DefaultEntity(type, world, uuid), LivingEntity {

    override var health: Double = 20.0
    override var maxHealth: Double = 20.0

    @Volatile
    override var lastDamageCause: DamageCause? = null

    private val potionEffects = ConcurrentHashMap<String, PotionEffect>()

    override suspend fun damage(amount: Double, source: DamageSource?) {
        if (isDead || isRemoved) return
        health = (health - amount).coerceAtLeast(0.0)
        lastDamageCause = source?.cause ?: DamageCause.CUSTOM
        logger.debug { "实体 $entityId 受到 $amount 伤害, 剩余生命: $health" }
        if (isDead) {
            kill()
        }
    }

    override suspend fun heal(amount: Double) {
        if (isDead || isRemoved) return
        health = (health + amount).coerceAtMost(maxHealth)
        logger.debug { "实体 $entityId 恢复 $amount 生命, 当前生命: $health" }
    }

    override suspend fun kill() {
        health = 0.0
        logger.debug { "实体 $entityId 死亡" }
    }

    override suspend fun addPotionEffect(effect: PotionEffect) {
        potionEffects[effect.type] = effect
    }

    override suspend fun removePotionEffect(effectType: String) {
        potionEffects.remove(effectType)
    }

    override fun getPotionEffects(): Collection<PotionEffect> = potionEffects.values

    override fun hasPotionEffect(effectType: String): Boolean = potionEffects.containsKey(effectType)
}

/**
 * 默认实体工厂
 */
class DefaultEntityFactory(private val world: World) : EntityFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Entity> create(type: EntityType, entityClass: Class<T>): T {
        val entity: Entity = when {
            LivingEntity::class.java.isAssignableFrom(entityClass) ->
                DefaultLivingEntity(type, world)
            else -> DefaultEntity(type, world)
        }
        return entity as T
    }

    override fun createLiving(type: String): LivingEntity {
        return DefaultLivingEntity(EntityType.MOB, world)
    }

    override fun createItem(item: Any): Entity {
        return DefaultEntity(EntityType.ITEM, world)
    }
}
