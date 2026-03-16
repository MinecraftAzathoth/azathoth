package com.azathoth.game.mechanics.ai.manager

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.controller.AIController
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * AI 管理器接口
 *
 * 批量管理多个 AI 实体的控制器。
 */
interface AIManager {

    /** 注册 AI 实体 */
    fun register(entity: LivingEntity, controller: AIController)

    /** 注销 AI 实体 */
    fun unregister(entity: LivingEntity)

    /** 批量 tick 所有 AI */
    suspend fun tickAll(tickNumber: Long)

    /** 获取实体的控制器 */
    fun getController(entity: LivingEntity): AIController?

    /** 暂停所有 AI */
    fun pauseAll()

    /** 恢复所有 AI */
    fun resumeAll()

    /** 获取活跃 AI 数量 */
    fun getActiveCount(): Int
}

/**
 * 默认 AI 管理器实现
 */
class DefaultAIManager : AIManager {

    private val controllers = ConcurrentHashMap<Int, AIController>()

    @Volatile
    private var paused = false

    override fun register(entity: LivingEntity, controller: AIController) {
        controllers[entity.entityId] = controller
        logger.debug { "注册 AI 实体: ${entity.entityId} (${entity.customName})" }
    }

    override fun unregister(entity: LivingEntity) {
        controllers.remove(entity.entityId)
        logger.debug { "注销 AI 实体: ${entity.entityId}" }
    }

    override suspend fun tickAll(tickNumber: Long) {
        if (paused) return

        // 清理已死亡/已移除的实体
        controllers.entries.removeIf { (_, ctrl) ->
            ctrl.entity.isDead || ctrl.entity.isRemoved
        }

        // 逐个 tick（后续可改为协程并发）
        for ((_, controller) in controllers) {
            controller.tick(tickNumber)
        }
    }

    override fun getController(entity: LivingEntity): AIController? {
        return controllers[entity.entityId]
    }

    override fun pauseAll() {
        paused = true
        controllers.values.forEach { it.pause() }
        logger.info { "所有 AI 已暂停" }
    }

    override fun resumeAll() {
        paused = false
        controllers.values.forEach { it.resume() }
        logger.info { "所有 AI 已恢复" }
    }

    override fun getActiveCount(): Int {
        return controllers.values.count { it.isActive }
    }
}
