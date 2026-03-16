package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 动作：自我治疗
 *
 * 恢复指定生命值，带冷却时间。冷却期间返回 FAILURE。
 */
class Heal(
    private val amount: Double = 10.0,
    private val cooldownTicks: Long = 100,
    name: String = "heal"
) : BehaviorNode(name) {

    private val lastHealKey get() = "_heal_${name}_last"

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val lastHeal = context.get<Long>(lastHealKey) ?: 0L
        if (context.currentTick - lastHeal < cooldownTicks) {
            return NodeStatus.FAILURE
        }

        val entity = context.entity
        if (entity.health >= entity.maxHealth) return NodeStatus.FAILURE

        entity.heal(amount)
        context.set(lastHealKey, context.currentTick)
        return NodeStatus.SUCCESS
    }

    override fun reset(context: BehaviorContext) {
        context.remove(lastHealKey)
    }
}
