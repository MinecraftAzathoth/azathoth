package com.azathoth.game.mechanics.ai.nodes.conditions

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 条件：自身血量是否低于指定百分比
 *
 * @param percent 百分比阈值（0.0~1.0），例如 0.3 表示 30%
 */
class IsHealthBelow(
    private val percent: Double,
    name: String = "isHealthBelow"
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val entity = context.entity
        val ratio = entity.health / entity.maxHealth
        return if (ratio < percent) NodeStatus.SUCCESS else NodeStatus.FAILURE
    }
}
