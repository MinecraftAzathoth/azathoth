package com.azathoth.game.mechanics.ai.nodes.conditions

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.perception.DefaultPerceptionSystem

/**
 * 条件：目标是否在指定范围内
 */
class IsTargetInRange(
    private val range: Double,
    name: String = "isTargetInRange"
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val target = context.get<LivingEntity>("target") ?: return NodeStatus.FAILURE
        val pos = context.entity.position
        val tPos = target.position
        val distSq = DefaultPerceptionSystem.distanceSq(
            pos.x, pos.y, pos.z, tPos.x, tPos.y, tPos.z
        )
        return if (distSq <= range * range) NodeStatus.SUCCESS else NodeStatus.FAILURE
    }
}
