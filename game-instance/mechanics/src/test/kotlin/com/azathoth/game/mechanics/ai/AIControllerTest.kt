package com.azathoth.game.mechanics.ai

import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.mechanics.ai.controller.AIController
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AIControllerTest {

    private lateinit var entity: LivingEntity
    private lateinit var controller: AIController

    @BeforeEach
    fun setup() {
        entity = mockk(relaxed = true) {
            every { health } returns 20.0
            every { isDead } returns false
        }

        val tree = behaviorTree("test_ai") {
            sequence {
                condition("alive") { !it.entity.isDead }
                action("patrol") {
                    it.set("patrolled", true)
                    NodeStatus.SUCCESS
                }
            }
        }

        controller = AIController(entity, tree)
    }

    @Test
    fun `tick 驱动行为树执行`() = runTest {
        controller.tick(1)
        assertTrue(controller.context.get<Boolean>("patrolled") == true)
    }

    @Test
    fun `实体死亡时不执行行为树`() = runTest {
        every { entity.isDead } returns true
        controller.tick(1)
        assertNull(controller.context.get<Boolean>("patrolled"))
    }

    @Test
    fun `pause 后不执行行为树`() = runTest {
        controller.pause()
        assertFalse(controller.isActive)

        controller.tick(1)
        assertNull(controller.context.get<Boolean>("patrolled"))
    }

    @Test
    fun `resume 后恢复执行`() = runTest {
        controller.pause()
        controller.resume()
        assertTrue(controller.isActive)

        controller.tick(1)
        assertTrue(controller.context.get<Boolean>("patrolled") == true)
    }

    @Test
    fun `reset 清空行为树状态`() = runTest {
        controller.tick(1)
        assertTrue(controller.context.get<Boolean>("patrolled") == true)

        controller.reset()
        assertNull(controller.context.get<Boolean>("patrolled"))
    }

    @Test
    fun `tick 更新 context 的 currentTick`() = runTest {
        controller.tick(42)
        assertEquals(42L, controller.context.currentTick)
    }

    @Test
    fun `deltaTime 在连续 tick 间更新`() = runTest {
        controller.tick(1)
        val dt = controller.context.deltaTime
        assertTrue(dt >= 0, "deltaTime 应为非负值")
    }
}
