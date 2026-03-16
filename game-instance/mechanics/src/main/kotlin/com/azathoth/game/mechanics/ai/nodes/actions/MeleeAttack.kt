package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.perception.DefaultPerceptionSystem

/**
 * 动作：近战攻击
 *
 * 对黑板中的目标造成伤害。目标必须在攻击范围内。
 */
class MeleeAttack(
    private val damage: Double = 5.0,
    private val range: Double = 2.5,
    name: String = "meleeAttack"
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val target = context.get<LivingEntity>("target") ?: return NodeStatus.FAILURE
        if (target.isDead || target.isRemoved) return NodeStatus.FAILURE

        val pos = context.entity.position
        val tPos = target.position
        val distSq = DefaultPerceptionSystem.distanceSq(
            pos.x, pos.y, pos.z, tPos.x, tPos.y, tPos.z
        )

        if (distSq > range * range) return NodeStatus.FAILURE

        target.damage(damage)
        return NodeStatus.SUCCESS
    }
}
