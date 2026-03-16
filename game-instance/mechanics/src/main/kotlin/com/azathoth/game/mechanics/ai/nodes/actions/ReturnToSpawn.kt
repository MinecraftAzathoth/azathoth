package com.azathoth.game.mechanics.ai.nodes.actions

import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.mechanics.ai.BehaviorNode
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import kotlin.math.sqrt

/**
 * 动作：返回出生点
 *
 * 向黑板中 "spawnPoint" 位置移动。到达后返回 SUCCESS。
 */
class ReturnToSpawn(
    private val speed: Double = 0.2,
    private val arrivalThreshold: Double = 1.5,
    name: String = "returnToSpawn"
) : BehaviorNode(name) {

    override suspend fun tick(context: BehaviorContext): NodeStatus {
        val spawnPoint = context.get<WorldPosition>("spawnPoint") ?: return NodeStatus.FAILURE

        val pos = context.entity.position
        val dx = spawnPoint.x - pos.x
        val dy = spawnPoint.y - pos.y
        val dz = spawnPoint.z - pos.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)

        if (dist <= arrivalThreshold) return NodeStatus.SUCCESS

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
