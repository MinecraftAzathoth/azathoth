package com.azathoth.game.mechanics.ai.manager

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.BehaviorTree
import com.azathoth.game.mechanics.ai.NodeStatus
import com.azathoth.game.mechanics.ai.controller.AIController
import com.azathoth.game.mechanics.ai.leaf.ActionNode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AIManagerTest {

    private lateinit var manager: DefaultAIManager

    @BeforeEach
    fun setup() {
        manager = DefaultAIManager()
    }

    @Test
    fun `register 和 getController`() {
        val (entity, controller) = createEntityWithController(1)
        manager.register(entity, controller)
        assertSame(controller, manager.getController(entity))
    }

    @Test
    fun `unregister 移除实体`() {
        val (entity, controller) = createEntityWithController(1)
        manager.register(entity, controller)
        manager.unregister(entity)
        assertNull(manager.getController(entity))
    }

    @Test
    fun `getActiveCount 返回活跃数量`() {
        val (e1, c1) = createEntityWithController(1)
        val (e2, c2) = createEntityWithController(2)
        manager.register(e1, c1)
        manager.register(e2, c2)
        assertEquals(2, manager.getActiveCount())
    }

    @Test
    fun `tickAll 驱动所有 AI`() = runTest {
        var ticked = false
        val entity = mockk<LivingEntity>(relaxed = true) {
            every { entityId } returns 1
            every { isDead } returns false
            every { isRemoved } returns false
            every { health } returns 20.0
            every { maxHealth } returns 20.0
        }
        val tree = BehaviorTree("test", ActionNode("test") {
            ticked = true
            NodeStatus.SUCCESS
        })
        val controller = AIController(entity, tree)

        manager.register(entity, controller)
        manager.tickAll(1)
        assertTrue(ticked)
    }

    @Test
    fun `tickAll 清理死亡实体`() = runTest {
        val entity = mockk<LivingEntity>(relaxed = true) {
            every { entityId } returns 1
            every { isDead } returns true
            every { isRemoved } returns false
            every { health } returns 0.0
            every { maxHealth } returns 20.0
        }
        val tree = BehaviorTree("test", ActionNode("test") { NodeStatus.SUCCESS })
        val controller = AIController(entity, tree)

        manager.register(entity, controller)
        manager.tickAll(1)
        // 死亡实体应被清理
        assertNull(manager.getController(entity))
    }

    @Test
    fun `pauseAll 和 resumeAll`() = runTest {
        val (entity, controller) = createEntityWithController(1)
        manager.register(entity, controller)

        manager.pauseAll()
        assertEquals(0, manager.getActiveCount())

        manager.resumeAll()
        assertEquals(1, manager.getActiveCount())
    }

    private fun createEntityWithController(id: Int): Pair<LivingEntity, AIController> {
        val entity = mockk<LivingEntity>(relaxed = true) {
            every { entityId } returns id
            every { isDead } returns false
            every { isRemoved } returns false
            every { health } returns 20.0
            every { maxHealth } returns 20.0
        }
        val tree = BehaviorTree("test_$id", ActionNode("idle") { NodeStatus.SUCCESS })
        return entity to AIController(entity, tree)
    }
}
