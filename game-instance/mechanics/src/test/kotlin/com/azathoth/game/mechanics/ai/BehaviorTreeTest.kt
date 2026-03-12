package com.azathoth.game.mechanics.ai

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.composite.Selector
import com.azathoth.game.mechanics.ai.composite.Sequence
import com.azathoth.game.mechanics.ai.decorator.Cooldown
import com.azathoth.game.mechanics.ai.decorator.Inverter
import com.azathoth.game.mechanics.ai.decorator.Repeater
import com.azathoth.game.mechanics.ai.leaf.ActionNode
import com.azathoth.game.mechanics.ai.leaf.ConditionNode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BehaviorTreeTest {

    private lateinit var entity: LivingEntity
    private lateinit var context: BehaviorContext

    @BeforeEach
    fun setup() {
        entity = mockk(relaxed = true) {
            every { health } returns 20.0
            every { maxHealth } returns 20.0
            every { isDead } returns false
        }
        context = BehaviorContext(entity)
    }

    // ─── 叶节点 ─────────────────────────────────────────────

    @Nested
    inner class LeafNodeTests {

        @Test
        fun `ActionNode 返回 lambda 的结果`() = runTest {
            val node = ActionNode("attack") { NodeStatus.SUCCESS }
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
        }

        @Test
        fun `ActionNode 可以返回 RUNNING`() = runTest {
            val node = ActionNode("move") { NodeStatus.RUNNING }
            assertEquals(NodeStatus.RUNNING, node.tick(context))
        }

        @Test
        fun `ConditionNode 条件为 true 返回 SUCCESS`() = runTest {
            val node = ConditionNode("has_target") { true }
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
        }

        @Test
        fun `ConditionNode 条件为 false 返回 FAILURE`() = runTest {
            val node = ConditionNode("has_target") { false }
            assertEquals(NodeStatus.FAILURE, node.tick(context))
        }

        @Test
        fun `ConditionNode 可以读取 context 数据`() = runTest {
            context.set("target", "zombie")
            val node = ConditionNode("has_target") { it.has("target") }
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
        }
    }

    // ─── 组合节点 ───────────────────────────────────────────

    @Nested
    inner class CompositeNodeTests {

        @Test
        fun `Sequence 所有子节点成功则返回 SUCCESS`() = runTest {
            val seq = Sequence("seq", listOf(
                ActionNode("a") { NodeStatus.SUCCESS },
                ActionNode("b") { NodeStatus.SUCCESS },
                ActionNode("c") { NodeStatus.SUCCESS }
            ))
            assertEquals(NodeStatus.SUCCESS, seq.tick(context))
        }

        @Test
        fun `Sequence 遇到 FAILURE 立即返回 FAILURE`() = runTest {
            var cExecuted = false
            val seq = Sequence("seq", listOf(
                ActionNode("a") { NodeStatus.SUCCESS },
                ActionNode("b") { NodeStatus.FAILURE },
                ActionNode("c") { cExecuted = true; NodeStatus.SUCCESS }
            ))
            assertEquals(NodeStatus.FAILURE, seq.tick(context))
            assertFalse(cExecuted)
        }

        @Test
        fun `Sequence 支持 RUNNING 断点续执行`() = runTest {
            var callCount = 0
            val seq = Sequence("seq", listOf(
                ActionNode("a") { NodeStatus.SUCCESS },
                ActionNode("b") {
                    callCount++
                    if (callCount == 1) NodeStatus.RUNNING else NodeStatus.SUCCESS
                },
                ActionNode("c") { NodeStatus.SUCCESS }
            ))

            assertEquals(NodeStatus.RUNNING, seq.tick(context))
            assertEquals(NodeStatus.SUCCESS, seq.tick(context))
        }

        @Test
        fun `Selector 遇到 SUCCESS 立即返回 SUCCESS`() = runTest {
            var cExecuted = false
            val sel = Selector("sel", listOf(
                ActionNode("a") { NodeStatus.FAILURE },
                ActionNode("b") { NodeStatus.SUCCESS },
                ActionNode("c") { cExecuted = true; NodeStatus.SUCCESS }
            ))
            assertEquals(NodeStatus.SUCCESS, sel.tick(context))
            assertFalse(cExecuted)
        }

        @Test
        fun `Selector 所有子节点失败则返回 FAILURE`() = runTest {
            val sel = Selector("sel", listOf(
                ActionNode("a") { NodeStatus.FAILURE },
                ActionNode("b") { NodeStatus.FAILURE }
            ))
            assertEquals(NodeStatus.FAILURE, sel.tick(context))
        }

        @Test
        fun `Selector 支持 RUNNING 断点续执行`() = runTest {
            var callCount = 0
            val sel = Selector("sel", listOf(
                ActionNode("a") { NodeStatus.FAILURE },
                ActionNode("b") {
                    callCount++
                    if (callCount == 1) NodeStatus.RUNNING else NodeStatus.SUCCESS
                }
            ))

            assertEquals(NodeStatus.RUNNING, sel.tick(context))
            assertEquals(NodeStatus.SUCCESS, sel.tick(context))
        }
    }

    // ─── 装饰器 ─────────────────────────────────────────────

    @Nested
    inner class DecoratorTests {

        @Test
        fun `Inverter 将 SUCCESS 转为 FAILURE`() = runTest {
            val inv = Inverter("inv", ActionNode("a") { NodeStatus.SUCCESS })
            assertEquals(NodeStatus.FAILURE, inv.tick(context))
        }

        @Test
        fun `Inverter 将 FAILURE 转为 SUCCESS`() = runTest {
            val inv = Inverter("inv", ActionNode("a") { NodeStatus.FAILURE })
            assertEquals(NodeStatus.SUCCESS, inv.tick(context))
        }

        @Test
        fun `Inverter 保持 RUNNING 不变`() = runTest {
            val inv = Inverter("inv", ActionNode("a") { NodeStatus.RUNNING })
            assertEquals(NodeStatus.RUNNING, inv.tick(context))
        }

        @Test
        fun `Repeater 重复指定次数后返回 SUCCESS`() = runTest {
            var count = 0
            val rep = Repeater("rep", ActionNode("a") { count++; NodeStatus.SUCCESS }, times = 3)

            assertEquals(NodeStatus.RUNNING, rep.tick(context))
            assertEquals(NodeStatus.RUNNING, rep.tick(context))
            assertEquals(NodeStatus.SUCCESS, rep.tick(context))
            assertEquals(3, count)
        }

        @Test
        fun `Repeater 子节点 FAILURE 时立即终止`() = runTest {
            val rep = Repeater("rep", ActionNode("a") { NodeStatus.FAILURE }, times = 3)
            assertEquals(NodeStatus.FAILURE, rep.tick(context))
        }

        @Test
        fun `Cooldown 冷却期间返回 FAILURE`() = runTest {
            val cd = Cooldown("cd", ActionNode("a") { NodeStatus.SUCCESS }, cooldownTicks = 5)
            context.currentTick = 10

            assertEquals(NodeStatus.SUCCESS, cd.tick(context))

            // 冷却期间
            context.currentTick = 12
            assertEquals(NodeStatus.FAILURE, cd.tick(context))

            // 冷却结束
            context.currentTick = 15
            assertEquals(NodeStatus.SUCCESS, cd.tick(context))
        }
    }

    // ─── DSL ────────────────────────────────────────────────

    @Nested
    inner class DslTests {

        @Test
        fun `DSL 构建简单行为树`() = runTest {
            val tree = behaviorTree("test") {
                selector {
                    condition("is_dead") { it.entity.isDead }
                    sequence("attack") {
                        condition("has_target") { it.has("target") }
                        action("do_attack") { NodeStatus.SUCCESS }
                    }
                    action("idle") { NodeStatus.SUCCESS }
                }
            }

            // 没有 target，selector 跳过 attack sequence，执行 idle
            val status = tree.tick(context)
            assertEquals(NodeStatus.SUCCESS, status)
        }

        @Test
        fun `DSL 支持装饰器嵌套`() = runTest {
            val tree = behaviorTree("test") {
                sequence {
                    inverter("not_dead") {
                        condition("is_dead") { it.entity.isDead }
                    }
                    action("patrol") { NodeStatus.SUCCESS }
                }
            }

            // entity 未死亡 → isDead=false → condition=FAILURE → inverter=SUCCESS → patrol 执行
            val status = tree.tick(context)
            assertEquals(NodeStatus.SUCCESS, status)
        }

        @Test
        fun `DSL 支持 cooldown 装饰器`() = runTest {
            val tree = behaviorTree("test") {
                selector {
                    cooldown("skill", ticks = 10) {
                        action("use_skill") { NodeStatus.SUCCESS }
                    }
                    action("basic_attack") { NodeStatus.SUCCESS }
                }
            }

            context.currentTick = 0
            assertEquals(NodeStatus.SUCCESS, tree.tick(context))

            // 冷却中，skill 失败，fallback 到 basic_attack
            context.currentTick = 5
            assertEquals(NodeStatus.SUCCESS, tree.tick(context))
        }
    }

    // ─── BehaviorContext ────────────────────────────────────

    @Nested
    inner class ContextTests {

        @Test
        fun `黑板数据读写`() {
            context.set("key", 42)
            assertEquals(42, context.get<Int>("key"))
            assertTrue(context.has("key"))

            context.remove("key")
            assertNull(context.get<Int>("key"))
            assertFalse(context.has("key"))
        }

        @Test
        fun `clear 清空所有数据`() {
            context.set("a", 1)
            context.set("b", 2)
            context.clear()
            assertFalse(context.has("a"))
            assertFalse(context.has("b"))
        }

        @Test
        fun `entity 引用正确`() {
            assertSame(entity, context.entity)
        }
    }
}
