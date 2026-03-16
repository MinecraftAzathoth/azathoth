package com.azathoth.game.mechanics.ai.mob

import com.azathoth.game.engine.entity.EntityType
import com.azathoth.game.mechanics.ai.BehaviorTree
import com.azathoth.game.mechanics.ai.perception.PerceptionSystem

/**
 * Mob 定义模板
 *
 * 描述一种 Mob 的静态属性和行为树工厂。
 */
data class MobDefinition(
    /** 唯一标识 */
    val id: String,
    /** 显示名称 */
    val name: String,
    /** 实体类型 */
    val entityType: EntityType = EntityType.MOB,
    /** 最大生命值 */
    val maxHealth: Double = 20.0,
    /** 攻击力 */
    val attack: Double = 5.0,
    /** 防御力 */
    val defense: Double = 0.0,
    /** 视野范围 */
    val sightRange: Double = 16.0,
    /** 移动速度 */
    val moveSpeed: Double = 0.2,
    /** 行为树工厂（每个实例创建独立的行为树） */
    val behaviorTreeFactory: (PerceptionSystem) -> BehaviorTree
)

/**
 * Mob 注册表接口
 */
interface MobRegistry {

    /** 注册 Mob 定义 */
    fun register(definition: MobDefinition)

    /** 获取 Mob 定义 */
    fun get(id: String): MobDefinition?

    /** 获取所有 Mob 定义 */
    fun getAll(): Collection<MobDefinition>

    /** 是否已注册 */
    fun contains(id: String): Boolean
}

/**
 * 默认 Mob 注册表实现
 */
class DefaultMobRegistry : MobRegistry {

    private val definitions = java.util.concurrent.ConcurrentHashMap<String, MobDefinition>()

    override fun register(definition: MobDefinition) {
        definitions[definition.id] = definition
    }

    override fun get(id: String): MobDefinition? = definitions[id]

    override fun getAll(): Collection<MobDefinition> = definitions.values

    override fun contains(id: String): Boolean = definitions.containsKey(id)
}
