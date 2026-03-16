package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import kotlin.math.sqrt

/**
 * 动作：巡逻
 *
 * 沿路径点循环移动。到达当前路径点后切换到下一个。
 * 所有路径点遍历完一轮后返回 SUCCESS，否则返回 RUNNING。
 */
class Patrol(
    private val waypoints: List<WorldPosition>,
    private val speed: Double = 0.15,
    private val arrivalThreshold: Double = 1.0,
    name: String = "patrol"
) : BehaviorNode(name) {

    private val waypointIndexKey get() = "_patrol_${name}_idx"

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        if (waypoints.isEmpty()) return NodeStatus.FAILURE

        val idx = context.get<Int>(waypointIndexKey) ?: 0
        val target = waypoints[idx]
        val pos = context.entity.position

        val dx = target.x - pos.x
        val dy = target.y - pos.y
        val dz = target.z - pos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)

        if (dist <= arrivalThreshold) {
            // 到达当前路径点，切换到下一个
            val nextIdx = (idx + 1) % waypoints.size
            context.set(waypointIndexKey, nextIdx)
            // 完成一轮巡逻
            return if (nextIdx == 0) NodeStatus.SUCCESS else NodeStatus.RUNNING
        }

        // 向路径点移动
        val factor = speed / dist
        context.entity.position = WorldPosition(
            x = pos.x + dx * factor,
            y = pos.y + dy * factor,
            z = pos.z + dz * factor,
            yaw = pos.yaw,
            pitch = pos.pitch
        )
        return NodeStatus.RUNNING
    }

    override fun reset(context: BehaviorContext) {
        context.remove(waypointIndexKey)
    }
}
