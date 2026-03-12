package com.azathoth.game.mechanics.ai.composite

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 序列节点（AND 逻辑）
 *
 * 依次执行子节点，直到某个子节点返回 FAILURE 或 RUNNING。
 * 如果所有子节点都返回 SUCCESS，则本节点返回 SUCCESS。
 * 支持 RUNNING 状态的断点续执行。
 */
class Sequence(
    name: String = "sequence",
    val children: List<BehaviorNode>
) : BehaviorNode(name) {

    private val runningIndexKey get() = "_sequence_${name}_running_idx"

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val startIndex = context.get<Int>(runningIndexKey) ?: 0

        for (i in startIndex until children.size) {
            val status = children[i].tick(context)
            when (status) {
                NodeStatus.FAILURE -> {
                    context.remove(runningIndexKey)
                    return NodeStatus.FAILURE
                }
                NodeStatus.RUNNING -> {
                    context.set(runningIndexKey, i)
                    return NodeStatus.RUNNING
                }
                NodeStatus.SUCCESS -> continue
            }
        }

        context.remove(runningIndexKey)
        return NodeStatus.SUCCESS
    }

    override fun reset(context: BehaviorContext) {
        context.remove(runningIndexKey)
        children.forEach { it.reset(context) }
    }
}
