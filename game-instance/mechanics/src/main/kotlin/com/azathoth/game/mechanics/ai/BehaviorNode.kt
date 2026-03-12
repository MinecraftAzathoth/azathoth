package com.azathoth.game.mechanics.ai

import com.azathoth.game.mechanics.ai.context.BehaviorContext

/**
 * 行为树节点状态
 */
enum class NodeStatus {
    /** 节点执行成功 */
    SUCCESS,
    /** 节点执行失败 */
    FAILURE,
    /** 节点正在执行中，需要后续 tick 继续 */
    RUNNING
}

/**
 * 行为树节点基类
 *
 * 所有节点无状态设计，运行时状态存储在 [BehaviorContext] 中。
 */
abstract class BehaviorNode(val name: String = "") {

    /**
     * 执行节点逻辑，返回节点状态
     */
    abstract suspend fun tick(context: BehaviorContext): NodeStatus

    /**
     * 重置节点状态（清理 context 中与此节点相关的数据）
     */
    open fun reset(context: BehaviorContext) {}
}
