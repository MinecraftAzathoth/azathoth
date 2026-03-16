package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.perception.DefaultPerceptionSystem

/**
 * 动作：远程攻击
 *
 * 对黑板中的目标造成远程伤害。目标必须在射程内。
 * 实际项目中可扩展为生成投射物实体。
 */
class RangedAttack(
    private val damage: Double = 3.0,
    private val range: Double = 16.0,
    private val projectileSpeed: Double = 1.0,
    name: String = "rangedAttack"
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

        // 简化实现：直接造成伤害（后续可替换为投射物系统）
        target.damage(damage)
        return NodeStatus.SUCCESS
    }
}
