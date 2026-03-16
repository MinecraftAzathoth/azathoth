package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.perception.PerceptionSystem

/**
 * 动作：通过感知系统寻找最近敌人，写入黑板 "target"
 */
class FindTarget(
    private val perception: PerceptionSystem,
    private val range: Double = 16.0,
    name: String = "findTarget"
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val enemy = perception.findNearestEnemy(context.entity, range)
        return if (enemy != null) {
            context.set("target", enemy)
            NodeStatus.SUCCESS
        } else {
            NodeStatus.FAILURE
        }
    }
}
