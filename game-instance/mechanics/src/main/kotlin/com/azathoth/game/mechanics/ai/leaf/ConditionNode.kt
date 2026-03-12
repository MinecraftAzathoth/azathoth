package com.azathoth.game.mechanics.ai.leaf

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 条件叶节点
 *
 * 检查条件是否满足，返回 SUCCESS 或 FAILURE。
 */
class ConditionNode(
    name: String = "condition",
    private val condition: suspend (BehaviorContext) -> Boolean
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus =
        if (condition(context)) NodeStatus.SUCCESS else NodeStatus.FAILURE
}
