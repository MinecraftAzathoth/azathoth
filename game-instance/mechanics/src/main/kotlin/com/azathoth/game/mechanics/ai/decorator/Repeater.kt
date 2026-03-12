package com.azathoth.game.mechanics.ai.decorator

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 重复装饰器
 *
 * 重复执行子节点指定次数。
 * - [times] <= 0 表示无限重复
 * - 子节点返回 RUNNING 时暂停计数，下次 tick 继续
 * - 子节点返回 FAILURE 时立即终止并返回 FAILURE
 */
class Repeater(
    name: String = "repeater",
    val child: BehaviorNode,
    val times: Int = -1
) : BehaviorNode(name) {

    private val countKey get() = "_repeater_${name}_count"

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val count = context.get<Int>(countKey) ?: 0

        val status = child.tick(context)
        return when (status) {
            NodeStatus.FAILURE -> {
                context.remove(countKey)
                NodeStatus.FAILURE
            }
            NodeStatus.RUNNING -> NodeStatus.RUNNING
            NodeStatus.SUCCESS -> {
                val newCount = count + 1
                if (times > 0 && newCount >= times) {
                    context.remove(countKey)
                    NodeStatus.SUCCESS
                } else {
                    context.set(countKey, newCount)
                    NodeStatus.RUNNING
                }
            }
        }
    }

    override fun reset(context: BehaviorContext) {
        context.remove(countKey)
        child.reset(context)
    }
}
