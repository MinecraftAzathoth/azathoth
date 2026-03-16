package com.azathoth.game.mechanics.ai.pathfinding

import com.azathoth.game.engine.world.WorldPosition
import kotlin.math.sqrt

/**
 * 路径节点
 */
data class PathNode(val position: WorldPosition)

/**
 * 路径
 */
data class Path(
    val nodes: List<PathNode>,
    val totalDistance: Double
) {
    /** 路径是否为空 */
    val isEmpty: Boolean get() = nodes.isEmpty()

    /** 获取下一个节点（索引从 0 开始） */
    fun getNode(index: Int): PathNode? = nodes.getOrNull(index)
}

/**
 * 寻路接口
 *
 * 抽象寻路逻辑，可替换不同实现（直线、A* 等）。
 */
interface Pathfinder {

    /** 查找从起点到终点的路径 */
    suspend fun findPath(from: WorldPosition, to: WorldPosition): Path?

    /** 判断两点之间是否可达 */
    fun isReachable(from: WorldPosition, to: WorldPosition): Boolean
}

/**
 * 简单直线寻路实现
 *
 * 将起点到终点按步长分割为路径节点。
 * 后续可替换为 A* 或 NavMesh 实现。
 */
class SimplePathfinder(
    /** 每个路径节点之间的步长 */
    private val stepSize: Double = 1.0
) : Pathfinder {

    override suspend fun findPath(from: WorldPosition, to: WorldPosition): Path? {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dz = to.z - from.z
        val dist = sqrt(dx * dx + dy * dy + dz * dz)

        if (dist < stepSize) {
            return Path(listOf(PathNode(to)), dist)
        }

        val steps = (dist / stepSize).toInt().coerceAtLeast(1)
        val nodes = (1..steps).map { i ->
            val t = i.toDouble() / steps
            PathNode(
                WorldPosition(
                    x = from.x + dx * t,
                    y = from.y + dy * t,
                    z = from.z + dz * t
                )
            )
        }

        return Path(nodes, dist)
    }

    override fun isReachable(from: WorldPosition, to: WorldPosition): Boolean {
        // 简单实现：始终可达（后续可加障碍物检测）
        return true
    }
}
