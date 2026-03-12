package com.azathoth.game.mechanics.ai.decorator

import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 冷却装饰器
 *
 * 子节点执行成功后进入冷却期，冷却期间直接返回 FAILURE。
 * [cooldownTicks] 为冷却的 tick 数。
 */
class Cooldown(
    name: String = "cooldown",
    val child: BehaviorNode,
    val cooldownTicks: Long
) : BehaviorNode(name) {

    private val lastSuccessTickKey get() = "_cooldown_${name}_last_success"

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val lastSuccess = context.get<Long>(lastSuccessTickKey)
        if (lastSuccess != null) {
            val elapsed = context.currentTick - lastSuccess
            if (elapsed < cooldownTicks) {
                return NodeStatus.FAILURE
            }
        }

        val status = child.tick(context)
        if (status == NodeStatus.SUCCESS) {
            context.set(lastSuccessTickKey, context.currentTick)
        }
        return status
    }

    override fun reset(context: BehaviorContext) {
        context.remove(lastSuccessTickKey)
        child.reset(context)
    }
}
