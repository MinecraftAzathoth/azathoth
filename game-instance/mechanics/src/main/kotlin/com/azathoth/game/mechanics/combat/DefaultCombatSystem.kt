package com.azathoth.game.mechanics.combat

import com.azathoth.game.engine.entity.DamageSource
import com.azathoth.game.engine.entity.DamageType
import com.azathoth.game.engine.entity.LivingEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

/**
 * 默认战斗属性
 */
data class DefaultCombatStats(
    override val attack: Double = 10.0,
    override val defense: Double = 5.0,
    override val critRate: Double = 0.1,
    override val critDamage: Double = 1.5,
    override val attackSpeed: Double = 1.0,
    override val lifeSteal: Double = 0.0,
    override val penetration: Double = 0.0,
    override val blockRate: Double = 0.0,
    override val dodgeRate: Double = 0.0
) : CombatStats

/**
 * 默认伤害上下文
 */
class DefaultDamageContext(
    override val attacker: LivingEntity?,
    override val defender: LivingEntity,
    override val source: DamageSource,
    override val baseDamage: Double,
    override val damageType: DamageType
) : DamageContext {
    override var isCritical: Boolean = false
    override var finalDamage: Double = baseDamage
    override var isBlocked: Boolean = false
    override var isDodged: Boolean = false
    override var isCancelled: Boolean = false
}

/**
 * 默认战斗系统实现
 *
 * 伤害计算管线:
 * 1. 基础伤害
 * 2. 应用修改器（按优先级排序）
 * 3. 暴击检测
 * 4. 防御减免
 * 5. 最终伤害
 */
class DefaultCombatSystem : CombatSystem {
    private val modifiers = ConcurrentHashMap<String, DamageModifier>()
    private val listeners = CopyOnWriteArrayList<CombatListener>()
    private val entityStats = ConcurrentHashMap<Int, CombatStats>()

    override fun calculateDamage(context: DamageContext): Double {
        if (context.isCancelled) return 0.0

        val attackerStats = context.attacker?.let { getCombatStats(it) }
        val defenderStats = getCombatStats(context.defender)

        // 1. 基础伤害已在 context 中设置
        context.finalDamage = context.baseDamage

        // 2. 应用修改器（按优先级排序）
        val sortedModifiers = modifiers.values.sortedBy { it.priority }
        for (modifier in sortedModifiers) {
            modifier.modify(context)
            if (context.isCancelled) return 0.0
        }

        // 3. 闪避检测
        if (Random.nextDouble() < defenderStats.dodgeRate) {
            context.isDodged = true
            context.finalDamage = 0.0
            return 0.0
        }

        // 4. 格挡检测
        if (Random.nextDouble() < defenderStats.blockRate) {
            context.isBlocked = true
            context.finalDamage *= 0.5
        }

        // 5. 暴击检测
        if (attackerStats != null && Random.nextDouble() < attackerStats.critRate) {
            context.isCritical = true
            context.finalDamage *= attackerStats.critDamage
        }

        // 6. 防御减免: damage = finalDamage * (100 / (100 + effectiveDefense))
        val penetration = attackerStats?.penetration ?: 0.0
        val effectiveDefense = (defenderStats.defense - penetration).coerceAtLeast(0.0)
        context.finalDamage *= (100.0 / (100.0 + effectiveDefense))

        // 确保伤害不为负
        context.finalDamage = context.finalDamage.coerceAtLeast(0.0)

        return context.finalDamage
    }

    override suspend fun applyDamage(context: DamageContext) {
        // 通知监听器 - 攻击前
        for (listener in listeners) {
            listener.onPreDamage(context)
            if (context.isCancelled) return
        }

        val damage = calculateDamage(context)
        if (context.isCancelled || context.isDodged) return

        // 应用伤害
        context.defender.damage(damage, context.source)
        logger.debug { "应用伤害: ${context.attacker?.entityId} -> ${context.defender.entityId}, 伤害=$damage, 暴击=${context.isCritical}" }

        // 生命偷取
        if (context.attacker != null) {
            val stats = getCombatStats(context.attacker!!)
            if (stats.lifeSteal > 0) {
                val healAmount = damage * stats.lifeSteal
                context.attacker!!.heal(healAmount)
            }
        }

        // 通知监听器 - 攻击后
        for (listener in listeners) {
            listener.onPostDamage(context)
        }

        // 检查死亡
        if (context.defender.isDead) {
            for (listener in listeners) {
                listener.onEntityDeath(context.defender, context.attacker)
            }
        }
    }

    override fun registerModifier(modifier: DamageModifier) {
        modifiers[modifier.name] = modifier
    }

    override fun unregisterModifier(name: String) {
        modifiers.remove(name)
    }

    override fun getCombatStats(entity: LivingEntity): CombatStats {
        return entityStats[entity.entityId] ?: DefaultCombatStats()
    }

    override fun canAttack(attacker: LivingEntity, target: LivingEntity): Boolean {
        if (attacker.isDead || target.isDead) return false
        if (attacker.isRemoved || target.isRemoved) return false
        if (attacker.entityId == target.entityId) return false
        return true
    }

    fun setCombatStats(entity: LivingEntity, stats: CombatStats) {
        entityStats[entity.entityId] = stats
    }

    fun addListener(listener: CombatListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: CombatListener) {
        listeners.remove(listener)
    }
}


