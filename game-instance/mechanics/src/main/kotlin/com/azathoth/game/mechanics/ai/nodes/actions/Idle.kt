package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import kotlin.random.Random

/**
 * 动作：空闲等待
 *
 * 等待随机时间后返回 SUCCESS。等待期间返回 RUNNING。
 */
class Idle(
    private val minTicks: Int = 20,
    private val maxTicks: Int = 60,
    name: String = "idle"
) : BehaviorNode(name) {

    private val waitEndKey get() = "_idle_${name}_end"

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val endTick = context.get<Long>(waitEndKey)
        if (endTick == null) {
            // 首次进入，设置等待结束时间
            val waitTicks = Random.nextInt(minTicks, maxTicks + 1)
            context.set(waitEndKey, context.currentTick + waitTicks)
            return NodeStatus.RUNNING
        }

        return if (context.currentTick >= endTick) {
            context.remove(waitEndKey)
            NodeStatus.SUCCESS
        } else {
            NodeStatus.RUNNING
        }
    }

    override fun reset(context: BehaviorContext) {
        context.remove(waitEndKey)
    }
}
