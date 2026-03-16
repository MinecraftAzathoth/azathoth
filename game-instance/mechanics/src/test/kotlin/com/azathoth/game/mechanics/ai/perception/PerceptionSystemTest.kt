package com.azathoth.game.mechanics.ai.perception

import com.azathoth.game.engine.entity.Entity
import com.azathoth.game.engine.entity.LivingEntity
import com.azathoth.game.engine.world.World
import com.azathoth.game.engine.world.WorldPosition
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PerceptionSystemTest {

    private lateinit var world: World
    private lateinit var perception: DefaultPerceptionSystem
    private lateinit var self: LivingEntity

    @BeforeEach
    fun setup() {
        world = mockk(relaxed = true)
        perception = DefaultPerceptionSystem(world)
        self = createEntity(1, WorldPosition(0.0, 0.0, 0.0, 0f, 0f))
    }

    @Test
    fun `findNearbyEntities 返回范围内的存活实体`() {
        val near = createEntity(2, WorldPosition(5.0, 0.0, 0.0))
        val far = createEntity(3, WorldPosition(100.0, 0.0, 0.0))
        val dead = createEntity(4, WorldPosition(3.0, 0.0, 0.0), isDead = true)

        every { world.getEntities() } returns listOf(self, near, far, dead)

        val result = perception.findNearbyEntities(self, 10.0)
        assertEquals(1, result.size)
        assertEquals(2, result[0].entityId)
    }

    @Test
    fun `findNearbyEntities 不包含自身`() {
        every { world.getEntities() } returns listOf(self)
        val result = perception.findNearbyEntities(self, 10.0)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `findNearestEnemy 返回最近的实体`() {
        val near = createEntity(2, WorldPosition(3.0, 0.0, 0.0))
        val nearer = createEntity(3, WorldPosition(1.0, 0.0, 0.0))

        every { world.getEntities() } returns listOf(self, near, nearer)

        val result = perception.findNearestEnemy(self, 10.0)
        assertNotNull(result)
        assertEquals(3, result!!.entityId)
    }

    @Test
    fun `findNearestEnemy 无敌人时返回 null`() {
        every { world.getEntities() } returns listOf(self)
        assertNull(perception.findNearestEnemy(self, 10.0))
    }

    @Test
    fun `isInSight 正前方目标可见`() {
        // yaw=0 朝南 (+Z)
        val entity = createEntity(1, WorldPosition(0.0, 0.0, 0.0, 0f, 0f))
        val target = createEntity(2, WorldPosition(0.0, 0.0, 10.0))
        assertTrue(perception.isInSight(entity, target, 120.0))
    }

    @Test
    fun `isInSight 背后目标不可见`() {
        val entity = createEntity(1, WorldPosition(0.0, 0.0, 0.0, 0f, 0f))
        val target = createEntity(2, WorldPosition(0.0, 0.0, -10.0))
        assertFalse(perception.isInSight(entity, target, 120.0))
    }

    @Test
    fun `getEntitiesInCone 过滤视野外实体`() {
        val inFront = createEntity(2, WorldPosition(0.0, 0.0, 5.0))
        val behind = createEntity(3, WorldPosition(0.0, 0.0, -5.0))

        every { world.getEntities() } returns listOf(self, inFront, behind)

        // self yaw=0 朝南
        val result = perception.getEntitiesInCone(self, 10.0, 90.0)
        assertEquals(1, result.size)
        assertEquals(2, result[0].entityId)
    }

    private fun createEntity(
        id: Int,
        pos: WorldPosition,
        isDead: Boolean = false,
        isRemoved: Boolean = false
    ): LivingEntity = mockk(relaxed = true) {
        every { entityId } returns id
        every { position } returns pos
        every { this@mockk.isDead } returns isDead
        every { this@mockk.isRemoved } returns isRemoved
    }
}
