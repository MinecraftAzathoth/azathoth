package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.perception.DefaultPerceptionSystem
import kotlin.math.sqrt

/**
 * 动作：向目标移动（追击）
 *
 * 每 tick 将实体位置向目标方向移动 [speed] 距离。
 * 到达攻击范围内返回 SUCCESS，否则返回 RUNNING。
 */
class ChaseTarget(
    private val speed: Double = 0.2,
    private val arrivalDistance: Double = 2.0,
    name: String = "chaseTarget"
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val target = context.get<LivingEntity>("target") ?: return NodeStatus.FAILURE
        if (target.isDead || target.isRemoved) return NodeStatus.FAILURE

        val pos = context.entity.position
        val tPos = target.position
        val dx = tPos.x - pos.x
        val dy = tPos.y - pos.y
        val dz = tPos.z - pos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)

        if (dist <= arrivalDistance) return NodeStatus.SUCCESS

        // 归一化方向并移动
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
}
