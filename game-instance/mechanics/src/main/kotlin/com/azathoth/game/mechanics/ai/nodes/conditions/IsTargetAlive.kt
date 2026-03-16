package com.azathoth.game.mechanics.ai.nodes.conditions

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 条件：目标是否存活
 */
class IsTargetAlive(name: String = "isTargetAlive") : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val target = context.get<LivingEntity>("target")
        return if (target != null && !target.isDead && !target.isRemoved) {
            NodeStatus.SUCCESS
        } else {
            NodeStatus.FAILURE
        }
    }
}
