package com.azathoth.game.mechanics.ai.nodes

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.world.World
import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.nodes.actions.*
import com.azathoth.game.mechanics.ai.nodes.conditions.*
import com.azathoth.game.mechanics.ai.perception.DefaultPerceptionSystem
import com.azathoth.game.mechanics.ai.perception.PerceptionSystem
import com.azathoth.game.mechanics.ai.threat.ThreatTable
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PrebuiltNodesTest {

    private lateinit var entity: LivingEntity
    private lateinit var context: BehaviorContext

    @BeforeEach
    fun setup() {
        entity = mockk(relaxed = true) {
            every { health } returns 20.0
            every { maxHealth } returns 20.0
            every { isDead } returns false
            every { isRemoved } returns false
            every { position } returns WorldPosition(0.0, 0.0, 0.0)
        }
        context = BehaviorContext(entity)
        context.currentTick = 100
    }

    // ─── 条件节点 ─────────────────────────────────────────

    @Nested
    inner class ConditionTests {

        @Test
        fun `HasTarget 有存活目标返回 SUCCESS`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { isDead } returns false
                every { isRemoved } returns false
            }
            context.set("target", target)
            assertEquals(NodeStatus.SUCCESS, HasTarget().tick(context))
        }

        @Test
        fun `HasTarget 无目标返回 FAILURE`() = runTest {
            assertEquals(NodeStatus.FAILURE, HasTarget().tick(context))
        }

        @Test
        fun `HasTarget 目标已死返回 FAILURE`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { isDead } returns true
                every { isRemoved } returns false
            }
            context.set("target", target)
            assertEquals(NodeStatus.FAILURE, HasTarget().tick(context))
        }

        @Test
        fun `IsTargetAlive 存活目标返回 SUCCESS`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { isDead } returns false
                every { isRemoved } returns false
            }
            context.set("target", target)
            assertEquals(NodeStatus.SUCCESS, IsTargetAlive().tick(context))
        }

        @Test
        fun `IsTargetInRange 目标在范围内返回 SUCCESS`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { position } returns WorldPosition(2.0, 0.0, 0.0)
            }
            context.set("target", target)
            assertEquals(NodeStatus.SUCCESS, IsTargetInRange(5.0).tick(context))
        }

        @Test
        fun `IsTargetInRange 目标超出范围返回 FAILURE`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { position } returns WorldPosition(100.0, 0.0, 0.0)
            }
            context.set("target", target)
            assertEquals(NodeStatus.FAILURE, IsTargetInRange(5.0).tick(context))
        }

        @Test
        fun `IsHealthBelow 血量低于阈值返回 SUCCESS`() = runTest {
            every { entity.health } returns 3.0
            every { entity.maxHealth } returns 20.0
            assertEquals(NodeStatus.SUCCESS, IsHealthBelow(0.2).tick(context))
        }

        @Test
        fun `IsHealthBelow 血量高于阈值返回 FAILURE`() = runTest {
            every { entity.health } returns 18.0
            every { entity.maxHealth } returns 20.0
            assertEquals(NodeStatus.FAILURE, IsHealthBelow(0.2).tick(context))
        }

        @Test
        fun `HasThreat 有仇恨返回 SUCCESS`() = runTest {
            val table = ThreatTable()
            val target = mockk<LivingEntity>(relaxed = true) {
                every { entityId } returns 99
                every { isDead } returns false
                every { isRemoved } returns false
            }
            table.addThreat(target, 10.0)
            context.set("threatTable", table)
            assertEquals(NodeStatus.SUCCESS, HasThreat().tick(context))
        }

        @Test
        fun `HasThreat 无仇恨返回 FAILURE`() = runTest {
            context.set("threatTable", ThreatTable())
            assertEquals(NodeStatus.FAILURE, HasThreat().tick(context))
        }
    }

    // ─── 动作节点 ─────────────────────────────────────────

    @Nested
    inner class ActionTests {

        @Test
        fun `FindTarget 找到敌人写入黑板`() = runTest {
            val enemy = mockk<LivingEntity>(relaxed = true) {
                every { entityId } returns 42
            }
            val perception = mockk<PerceptionSystem> {
                every { findNearestEnemy(entity, 16.0) } returns enemy
            }

            val node = FindTarget(perception, 16.0)
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
            assertSame(enemy, context.get<LivingEntity>("target"))
        }

        @Test
        fun `FindTarget 无敌人返回 FAILURE`() = runTest {
            val perception = mockk<PerceptionSystem> {
                every { findNearestEnemy(entity, 16.0) } returns null
            }
            assertEquals(NodeStatus.FAILURE, FindTarget(perception, 16.0).tick(context))
        }

        @Test
        fun `MeleeAttack 在范围内造成伤害`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { isDead } returns false
                every { isRemoved } returns false
                every { position } returns WorldPosition(1.0, 0.0, 0.0)
            }
            context.set("target", target)

            val node = MeleeAttack(damage = 5.0, range = 3.0)
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
            coVerify { target.damage(5.0) }
        }

        @Test
        fun `MeleeAttack 超出范围返回 FAILURE`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { isDead } returns false
                every { isRemoved } returns false
                every { position } returns WorldPosition(100.0, 0.0, 0.0)
            }
            context.set("target", target)
            assertEquals(NodeStatus.FAILURE, MeleeAttack(damage = 5.0, range = 3.0).tick(context))
        }

        @Test
        fun `Idle 等待后返回 SUCCESS`() = runTest {
            val node = Idle(minTicks = 5, maxTicks = 5)
            context.currentTick = 100

            assertEquals(NodeStatus.RUNNING, node.tick(context))

            context.currentTick = 105
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
        }

        @Test
        fun `Heal 恢复生命值`() = runTest {
            every { entity.health } returns 10.0
            every { entity.maxHealth } returns 20.0

            val node = Heal(amount = 5.0, cooldownTicks = 50)
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
            coVerify { entity.heal(5.0) }
        }

        @Test
        fun `Heal 冷却期间返回 FAILURE`() = runTest {
            every { entity.health } returns 10.0
            every { entity.maxHealth } returns 20.0

            val node = Heal(amount = 5.0, cooldownTicks = 50)
            context.currentTick = 100
            assertEquals(NodeStatus.SUCCESS, node.tick(context))

            context.currentTick = 120
            assertEquals(NodeStatus.FAILURE, node.tick(context))
        }

        @Test
        fun `Heal 满血返回 FAILURE`() = runTest {
            every { entity.health } returns 20.0
            every { entity.maxHealth } returns 20.0

            assertEquals(NodeStatus.FAILURE, Heal().tick(context))
        }

        @Test
        fun `ChaseTarget 向目标移动`() = runTest {
            var currentPos = WorldPosition(0.0, 0.0, 0.0)
            every { entity.position } answers { currentPos }
            every { entity.position = any() } answers { currentPos = firstArg() }

            val target = mockk<LivingEntity>(relaxed = true) {
                every { isDead } returns false
                every { isRemoved } returns false
                every { position } returns WorldPosition(10.0, 0.0, 0.0)
            }
            context.set("target", target)

            val node = ChaseTarget(speed = 1.0, arrivalDistance = 2.0)
            assertEquals(NodeStatus.RUNNING, node.tick(context))
            assertTrue(currentPos.x > 0.0) // 向目标移动了
        }

        @Test
        fun `Patrol 沿路径点移动`() = runTest {
            var currentPos = WorldPosition(0.0, 0.0, 0.0)
            every { entity.position } answers { currentPos }
            every { entity.position = any() } answers { currentPos = firstArg() }

            val waypoints = listOf(
                WorldPosition(5.0, 0.0, 0.0),
                WorldPosition(5.0, 0.0, 5.0)
            )
            val node = Patrol(waypoints, speed = 10.0, arrivalThreshold = 1.0)

            // 第一次 tick 向第一个路径点移动
            val status = node.tick(context)
            assertTrue(status == NodeStatus.RUNNING || status == NodeStatus.SUCCESS)
        }

        @Test
        fun `RangedAttack 在射程内造成伤害`() = runTest {
            val target = mockk<LivingEntity>(relaxed = true) {
                every { isDead } returns false
                every { isRemoved } returns false
                every { position } returns WorldPosition(5.0, 0.0, 0.0)
            }
            context.set("target", target)

            val node = RangedAttack(damage = 3.0, range = 16.0)
            assertEquals(NodeStatus.SUCCESS, node.tick(context))
            coVerify { target.damage(3.0) }
        }

        @Test
        fun `Flee 远离目标`() = runTest {
            var currentPos = WorldPosition(5.0, 0.0, 0.0)
            every { entity.position } answers { currentPos }
            every { entity.position = any() } answers { currentPos = firstArg() }

            val target = mockk<LivingEntity>(relaxed = true) {
                every { position } returns WorldPosition(0.0, 0.0, 0.0)
            }
            context.set("target", target)

            val node = Flee(speed = 1.0, safeDistance = 20.0)
            assertEquals(NodeStatus.RUNNING, node.tick(context))
            assertTrue(currentPos.x > 5.0) // 远离了目标
        }

        @Test
        fun `ReturnToSpawn 向出生点移动`() = runTest {
            var currentPos = WorldPosition(10.0, 0.0, 0.0)
            every { entity.position } answers { currentPos }
            every { entity.position = any() } answers { currentPos = firstArg() }

            context.set("spawnPoint", WorldPosition(0.0, 0.0, 0.0))

            val node = ReturnToSpawn(speed = 1.0, arrivalThreshold = 1.0)
            assertEquals(NodeStatus.RUNNING, node.tick(context))
            assertTrue(currentPos.x < 10.0) // 向出生点移动了
        }
    }
}
