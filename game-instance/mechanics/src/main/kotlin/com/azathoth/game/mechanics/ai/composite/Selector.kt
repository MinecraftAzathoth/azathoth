package com.azathoth.game.mechanics.ai.composite

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 选择节点（OR 逻辑）
 *
 * 依次执行子节点，直到某个子节点返回 SUCCESS 或 RUNNING。
 * 如果所有子节点都返回 FAILURE，则本节点返回 FAILURE。
 * 支持 RUNNING 状态的断点续执行。
 */
class Selector(
    name: String = "selector",
    val children: List<BehaviorNode>
) : BehaviorNode(name) {

    private val runningIndexKey get() = "_selector_${name}_running_idx"

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val startIndex = context.get<Int>(runningIndexKey) ?: 0

        for (i in startIndex until children.size) {
            val status = children[i].tick(context)
            when (status) {
                NodeStatus.SUCCESS -> {
                    context.remove(runningIndexKey)
                    return NodeStatus.SUCCESS
                }
                NodeStatus.RUNNING -> {
                    context.set(runningIndexKey, i)
                    return NodeStatus.RUNNING
                }
                NodeStatus.FAILURE -> continue
            }
        }

        context.remove(runningIndexKey)
        return NodeStatus.FAILURE
    }

    override fun reset(context: BehaviorContext) {
        context.remove(runningIndexKey)
        children.forEach { it.reset(context) }
    }
}
