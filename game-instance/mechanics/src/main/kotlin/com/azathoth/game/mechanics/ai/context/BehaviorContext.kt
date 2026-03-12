package com.azathoth.game.mechanics.ai.context

import com.azathoth.game.engine.entity.LivingEntity

/**
 * 行为上下文（黑板模式）
 *
 * 在行为树节点间共享数据，所有运行时状态存储于此。
 */
class BehaviorContext(
    /** 行为树绑定的实体 */
    val entity: LivingEntity
) {
    /** 黑板数据存储 */
    private val blackboard = mutableMapOf<String, Any?>()

    /** 当前 tick 编号 */
    var currentTick: Long = 0L
        internal set

    /** 距上次 tick 的时间间隔（毫秒） */
    var deltaTime: Long = 50L
        internal set

    /**
     * 获取黑板数据
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? = blackboard[key] as? T

    /**
     * 设置黑板数据
     */
    fun set(key: String, value: Any?) {
        blackboard[key] = value
    }

    /**
     * 移除黑板数据
     */
    fun remove(key: String): Any? = blackboard.remove(key)

    /**
     * 检查黑板中是否存在指定 key
     */
    fun has(key: String): Boolean = blackboard.containsKey(key)

    /**
     * 清空黑板数据
     */
    fun clear() {
        blackboard.clear()
    }
}
