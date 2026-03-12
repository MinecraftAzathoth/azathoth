package com.azathoth.game.mechanics.ai.decorator

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 取反装饰器
 *
 * 将子节点的 SUCCESS 转为 FAILURE，FAILURE 转为 SUCCESS。
 * RUNNING 状态保持不变。
 */
class Inverter(
    name: String = "inverter",
    val child: BehaviorNode
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus =
        when (child.tick(context)) {
            NodeStatus.SUCCESS -> NodeStatus.FAILURE
            NodeStatus.FAILURE -> NodeStatus.SUCCESS
            NodeStatus.RUNNING -> NodeStatus.RUNNING
        }

    override fun reset(context: BehaviorContext) {
        child.reset(context)
    }
}
