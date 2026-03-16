package com.azathoth.game.mechanics.ai.templates

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.world.World
import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.context.BehaviorContext
import com.azathoth.game.mechanics.ai.perception.DefaultPerceptionSystem
import com.azathoth.game.mechanics.ai.threat.ThreatTable
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BehaviorTemplatesTest {

    private lateinit var world: World
    private lateinit var perception: DefaultPerceptionSystem
    private lateinit var entity: LivingEntity
    private lateinit var context: BehaviorContext

    @BeforeEach
    fun setup() {
        world = mockk(relaxed = true)
        perception = DefaultPerceptionSystem(world)
        entity = mockk(relaxed = true) {
            every { entityId } returns 1
            every { health } returns 20.0
            every { maxHealth } returns 20.0
            every { isDead } returns false
            every { isRemoved } returns false
            every { position } returns WorldPosition(0.0, 0.0, 0.0)
        }
        context = BehaviorContext(entity)
        context.currentTick = 1
    }

    @Test
    fun `aggressiveMelee 无敌人时空闲`() = runTest {
        every { world.getEntities() } returns listOf(entity)

        val tree = BehaviorTemplates.aggressiveMelee(perception)
        val status = tree.tick(context)
        // 无敌人 → FindTarget 失败 → combatLoop 失败 → Idle 返回 RUNNING
        assertEquals(NodeStatus.RUNNING, status)
    }

    @Test
    fun `aggressiveMelee 发现敌人后追击`() = runTest {
        val enemy = mockk<LivingEntity>(relaxed = true) {
            every { entityId } returns 2
            every { isDead } returns false
            every { isRemoved } returns false
            every { position } returns WorldPosition(10.0, 0.0, 0.0)
            every { health } returns 20.0
            every { maxHealth } returns 20.0
        }
        every { world.getEntities() } returns listOf(entity, enemy)

        val tree = BehaviorTemplates.aggressiveMelee(perception)
        val status = tree.tick(context)
        // 发现敌人 → 追击（距离 10 > 攻击范围 2.5）→ RUNNING
        assertEquals(NodeStatus.RUNNING, status)
    }

    @Test
    fun `aggressiveMelee 低血量时逃跑`() = runTest {
        every { entity.health } returns 2.0
        every { entity.maxHealth } returns 20.0

        val enemy = mockk<LivingEntity>(relaxed = true) {
            every { entityId } returns 2
            every { isDead } returns false
            every { isRemoved } returns false
            every { position } returns WorldPosition(5.0, 0.0, 0.0)
        }
        context.set("target", enemy)

        val tree = BehaviorTemplates.aggressiveMelee(perception)
        val status = tree.tick(context)
        // 血量 10% < 20% → 逃跑 → RUNNING
        assertEquals(NodeStatus.RUNNING, status)
    }

    @Test
    fun `aggressiveRanged 无敌人时空闲`() = runTest {
        every { world.getEntities() } returns listOf(entity)

        val tree = BehaviorTemplates.aggressiveRanged(perception)
        val status = tree.tick(context)
        assertEquals(NodeStatus.RUNNING, status)
    }

    @Test
    fun `passiveWanderer 正常巡逻`() = runTest {
        var currentPos = WorldPosition(0.0, 0.0, 0.0)
        every { entity.position } answers { currentPos }
        every { entity.position = any() } answers { currentPos = firstArg() }

        val waypoints = listOf(
            WorldPosition(10.0, 0.0, 0.0),
            WorldPosition(10.0, 0.0, 10.0)
        )
        val tree = BehaviorTemplates.passiveWanderer(waypoints)
        val status = tree.tick(context)
        // 巡逻中 → RUNNING
        assertEquals(NodeStatus.RUNNING, status)
    }

    @Test
    fun `passiveWanderer 有仇恨时逃跑`() = runTest {
        var currentPos = WorldPosition(0.0, 0.0, 0.0)
        every { entity.position } answers { currentPos }
        every { entity.position = any() } answers { currentPos = firstArg() }

        val attacker = mockk<LivingEntity>(relaxed = true) {
            every { entityId } returns 99
            every { isDead } returns false
            every { isRemoved } returns false
            every { position } returns WorldPosition(3.0, 0.0, 0.0)
        }
        val threatTable = ThreatTable()
        threatTable.addThreat(attacker, 10.0)
        context.set("threatTable", threatTable)
        context.set("target", attacker)

        val tree = BehaviorTemplates.passiveWanderer(listOf(WorldPosition(10.0, 0.0, 0.0)))
        val status = tree.tick(context)
        // 有仇恨 → 逃跑 → RUNNING
        assertEquals(NodeStatus.RUNNING, status)
    }
}
