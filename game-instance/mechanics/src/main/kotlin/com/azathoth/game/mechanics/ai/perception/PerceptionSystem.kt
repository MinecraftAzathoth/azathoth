package com.azathoth.game.mechanics.ai.perception

import com.azathoth.game.engine.entity.Entity
import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.world.World
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 感知配置
 */
data class PerceptionConfig(
    /** 视野范围 */
    val sightRange: Double = 16.0,
    /** 听觉范围 */
    val hearRange: Double = 8.0,
    /** 视野角度（度） */
    val fov: Double = 120.0
)

/**
 * 感知系统接口
 *
 * 提供实体对周围环境的感知能力：范围检测、视野判断等。
 */
interface PerceptionSystem {

    /** 查找范围内的所有存活生物实体 */
    fun findNearbyEntities(entity: LivingEntity, range: Double): List<LivingEntity>

    /** 查找最近的敌方实体 */
    fun findNearestEnemy(entity: LivingEntity, range: Double): LivingEntity?

    /** 判断目标是否在视野内 */
    fun isInSight(entity: LivingEntity, target: LivingEntity, fov: Double = 120.0): Boolean

    /** 获取锥形范围内的实体 */
    fun getEntitiesInCone(entity: LivingEntity, range: Double, angle: Double): List<LivingEntity>
}

/**
 * 默认感知系统实现
 *
 * 基于 [World.getEntities] 进行距离和角度过滤。
 */
class DefaultPerceptionSystem(private val world: World) : PerceptionSystem {

    override fun findNearbyEntities(entity: LivingEntity, range: Double): List<LivingEntity> {
        val pos = entity.position
        val rangeSq = range * range
        return world.getEntities()
            .asSequence()
            .filterIsInstance<LivingEntity>()
            .filter { it.entityId != entity.entityId && !it.isDead && !it.isRemoved }
            .filter { distanceSq(pos.x, pos.y, pos.z, it.position.x, it.position.y, it.position.z) <= rangeSq }
            .toList()
    }

    override fun findNearestEnemy(entity: LivingEntity, range: Double): LivingEntity? {
        val pos = entity.position
        return findNearbyEntities(entity, range)
            .minByOrNull { distanceSq(pos.x, pos.y, pos.z, it.position.x, it.position.y, it.position.z) }
    }

    override fun isInSight(entity: LivingEntity, target: LivingEntity, fov: Double): Boolean {
        val pos = entity.position
        val tPos = target.position
        val dx = tPos.x - pos.x
        val dz = tPos.z - pos.z

        // 实体朝向（yaw 角度转弧度，Minecraft 坐标系：yaw=0 朝南 +Z）
        val yawRad = Math.toRadians(-entity.position.yaw.toDouble())
        val facingX = -sin(yawRad)
        val facingZ = cos(yawRad)

        val dot = facingX * dx + facingZ * dz
        val mag = sqrt(dx * dx + dz * dz)
        if (mag < 1e-6) return true // 重叠位置视为可见

        val cosAngle = dot / mag
        val halfFovRad = Math.toRadians(fov / 2.0)
        return cosAngle >= cos(halfFovRad)
    }

    override fun getEntitiesInCone(entity: LivingEntity, range: Double, angle: Double): List<LivingEntity> {
        return findNearbyEntities(entity, range).filter { isInSight(entity, it, angle) }
    }

    companion object {
        /** 距离平方（避免开方） */
        fun distanceSq(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
            val dx = x2 - x1
            val dy = y2 - y1
            val dz = z2 - z1
            return dx * dx + dy * dy + dz * dz
        }

        /** 距离 */
        fun distance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
            return sqrt(distanceSq(x1, y1, z1, x2, y2, z2))
        }
    }
}
