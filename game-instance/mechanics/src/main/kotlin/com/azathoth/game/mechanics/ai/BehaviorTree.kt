package com.azathoth.game.mechanics.ai

import com.azathoth.game.mechanics.ai.composite.Selector
import com.azathoth.game.mechanics.ai.composite.Sequence
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.decorator.Cooldown
import com.azathoth.game.mechanics.ai.decorator.Inverter
import com.azathoth.game.mechanics.ai.decorator.Repeater
import com.azathoth.game.mechanics.ai.leaf.ActionNode
import com.azathoth.game.mechanics.ai.leaf.ConditionNode

/**
 * 行为树
 *
 * 封装根节点，提供 tick 驱动入口。
 */
class BehaviorTree(
    val name: String,
    val root: BehaviorNode
) {
    /**
     * 执行一次行为树 tick
     */
    suspend fun tick(context: BehaviorContext): NodeStatus = root.tick(context)

    /**
     * 重置行为树状态
     */
    fun reset(context: BehaviorContext) {
        root.reset(context)
    }
}

// ─── DSL ────────────────────────────────────────────────────────────────

/**
 * 行为树 DSL 构建器
 */
@DslMarker
annotation class BehaviorTreeDsl

/**
 * 行为树 DSL 入口
 */
fun behaviorTree(name: String = "root", block: BehaviorTreeBuilder.() -> Unit): BehaviorTree {
    val builder = BehaviorTreeBuilder(name)
    builder.block()
    return builder.build()
}

@BehaviorTreeDsl
class BehaviorTreeBuilder(private val name: String) {
    private var root: BehaviorNode? = null

    fun selector(name: String = "selector", block: CompositeBuilder.() -> Unit) {
        root = CompositeBuilder(name, CompositeType.SELECTOR).apply(block).build()
    }

    fun sequence(name: String = "sequence", block: CompositeBuilder.() -> Unit) {
        root = CompositeBuilder(name, CompositeType.SEQUENCE).apply(block).build()
    }

    fun build(): BehaviorTree = BehaviorTree(name, root ?: error("行为树必须有一个根节点"))
}

enum class CompositeType { SELECTOR, SEQUENCE }

@BehaviorTreeDsl
class CompositeBuilder(private val name: String, private val type: CompositeType) {
    private val children = mutableListOf<BehaviorNode>()

    fun action(name: String = "action", action: suspend (BehaviorContext) -> NodeStatus) {
        children += ActionNode(name, action)
    }

    fun condition(name: String = "condition", condition: suspend (BehaviorContext) -> Boolean) {
        children += ConditionNode(name, condition)
    }

    fun selector(name: String = "selector", block: CompositeBuilder.() -> Unit) {
        children += CompositeBuilder(name, CompositeType.SELECTOR).apply(block).build()
    }

    fun sequence(name: String = "sequence", block: CompositeBuilder.() -> Unit) {
        children += CompositeBuilder(name, CompositeType.SEQUENCE).apply(block).build()
    }

    fun inverter(name: String = "inverter", block: SingleChildBuilder.() -> Unit) {
        val child = SingleChildBuilder(name).apply(block).build()
        children += Inverter(name, child)
    }

    fun repeater(name: String = "repeater", times: Int = -1, block: SingleChildBuilder.() -> Unit) {
        val child = SingleChildBuilder(name).apply(block).build()
        children += Repeater(name, child, times)
    }

    fun cooldown(name: String = "cooldown", ticks: Long, block: SingleChildBuilder.() -> Unit) {
        val child = SingleChildBuilder(name).apply(block).build()
        children += Cooldown(name, child, ticks)
    }

    /**
     * 直接添加预制的行为节点实例
     */
    fun node(behaviorNode: BehaviorNode) {
        children += behaviorNode
    }

    fun build(): BehaviorNode = when (type) {
        CompositeType.SELECTOR -> Selector(name, children.toList())
        CompositeType.SEQUENCE -> Sequence(name, children.toList())
    }
}

@BehaviorTreeDsl
class SingleChildBuilder(private val name: String) {
    private var child: BehaviorNode? = null

    fun action(name: String = "action", action: suspend (BehaviorContext) -> NodeStatus) {
        child = ActionNode(name, action)
    }

    fun condition(name: String = "condition", condition: suspend (BehaviorContext) -> Boolean) {
        child = ConditionNode(name, condition)
    }

    fun selector(name: String = "selector", block: CompositeBuilder.() -> Unit) {
        child = CompositeBuilder(name, CompositeType.SELECTOR).apply(block).build()
    }

    fun sequence(name: String = "sequence", block: CompositeBuilder.() -> Unit) {
        child = CompositeBuilder(name, CompositeType.SEQUENCE).apply(block).build()
    }

    fun build(): BehaviorNode = child ?: error("装饰器节点 '$name' 必须有一个子节点")
}
