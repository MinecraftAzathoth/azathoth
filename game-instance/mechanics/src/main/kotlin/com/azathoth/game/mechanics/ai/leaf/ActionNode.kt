package com.azathoth.game.mechanics.ai.leaf

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 动作叶节点
 *
 * 执行具体的行为动作，通过 lambda 定义行为逻辑。
 */
class ActionNode(
    name: String = "action",
    private val action: suspend (BehaviorContext) -> NodeStatus
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus = action(context)
}
