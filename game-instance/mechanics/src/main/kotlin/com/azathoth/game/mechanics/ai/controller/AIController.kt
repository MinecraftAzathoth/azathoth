package com.azathoth.game.mechanics.ai.controller

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.BehaviorTree
import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * AI 控制器
 *
 * 将行为树绑定到 [LivingEntity]，每 tick 驱动行为树执行。
 */
class AIController(
    val entity: LivingEntity,
    val behaviorTree: BehaviorTree
) {
    val context = BehaviorContext(entity)

    private var lastTickTime = System.currentTimeMillis()
    private var active = true

    /**
     * 是否激活
     */
    val isActive: Boolean get() = active

    /**
     * 每 tick 调用，驱动行为树执行
     */
    suspend fun tick(tickNumber: Long) {
        if (!active || entity.isDead) return

        val now = System.currentTimeMillis()
        context.deltaTime = now - lastTickTime
        context.currentTick = tickNumber
        lastTickTime = now

        behaviorTree.tick(context)
    }

    /**
     * 暂停 AI
     */
    fun pause() {
        active = false
    }

    /**
     * 恢复 AI
     */
    fun resume() {
        active = true
        lastTickTime = System.currentTimeMillis()
    }

    /**
     * 重置行为树状态
     */
    fun reset() {
        behaviorTree.reset(context)
        context.clear()
    }
}
