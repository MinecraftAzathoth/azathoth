package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import kotlin.math.sqrt

/**
 * 动作：逃跑
 *
 * 远离黑板中的目标。到达安全距离后返回 SUCCESS。
 */
class Flee(
    private val speed: Double = 0.25,
    private val safeDistance: Double = 20.0,
    name: String = "flee"
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val target = context.get<LivingEntity>("target") ?: return NodeStatus.FAILURE

        val pos = context.entity.position
        val tPos = target.position
        val dx = pos.x - tPos.x
        val dy = pos.y - tPos.y
        val dz = pos.z - tPos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)

        if (dist >= safeDistance) return NodeStatus.SUCCESS

        // 远离方向移动
        if (dist < 1e-6) {
            // 重叠时随机方向逃跑
            context.entity.position = WorldPosition(
                x = pos.x + speed,
                y = pos.y,
                z = pos.z + speed,
                yaw = pos.yaw,
                pitch = pos.pitch
            )
        } else {
            val factor = speed / dist
            context.entity.position = WorldPosition(
                x = pos.x + dx * factor,
                y = pos.y + dy * factor,
                z = pos.z + dz * factor,
                yaw = pos.yaw,
                pitch = pos.pitch
            )
        }
        return NodeStatus.RUNNING
    }
}
