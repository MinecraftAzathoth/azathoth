package com.azathoth.game.engine.entity

import com.azathoth.game.engine.world.DefaultWorldManager
import com.azathoth.game.engine.world.WorldPosition
import com.azathoth.game.engine.world.WorldType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultEntityTest {
    private lateinit var worldManager: DefaultWorldManager

    @BeforeEach
    fun setup() = runTest {
        worldManager = DefaultWorldManager()
        worldManager.createWorld("test", WorldType.TEST)
    }

    private fun createLivingEntity(): DefaultLivingEntity {
        val world = worldManager.getDefaultWorld()
        return DefaultLivingEntity(EntityType.MOB, world)
    }

    @Test
    fun `entity should have unique id`() {
        val e1 = createLivingEntity()
        val e2 = createLivingEntity()
        assertNotEquals(e1.entityId, e2.entityId)
    }

    @Test
    fun `entity metadata should work`() {
        val entity = createLivingEntity()
        entity.setMetadata("key", "value")
        assertEquals("value", entity.getMetadata("key"))

        entity.removeMetadata("key")
        assertNull(entity.getMetadata("key"))
    }

    @Test
    fun `teleport should update position`() = runTest {
        val entity = createLivingEntity()
        val pos = WorldPosition(10.0, 20.0, 30.0)
        entity.teleport(pos)
        assertEquals(pos, entity.position)
    }

    @Test
    fun `damage should reduce health`() = runTest {
        val entity = createLivingEntity()
        entity.maxHealth = 100.0
        entity.health = 100.0

        entity.damage(30.0)
        assertEquals(70.0, entity.health)
    }

    @Test
    fun `damage should not go below zero`() = runTest {
        val entity = createLivingEntity()
        entity.health = 10.0
        entity.damage(50.0)
        assertEquals(0.0, entity.health)
        assertTrue(entity.isDead)
    }

    @Test
    fun `heal should restore health`() = runTest {
        val entity = createLivingEntity()
        entity.maxHealth = 100.0
        entity.health = 50.0

        entity.heal(30.0)
        assertEquals(80.0, entity.health)
    }

    @Test
    fun `heal should not exceed max health`() = runTest {
        val entity = createLivingEntity()
        entity.maxHealth = 100.0
        entity.health = 90.0

        entity.heal(50.0)
        assertEquals(100.0, entity.health)
    }

    @Test
    fun `kill should set health to zero`() = runTest {
        val entity = createLivingEntity()
        entity.kill()
        assertEquals(0.0, entity.health)
        assertTrue(entity.isDead)
    }

    @Test
    fun `potion effects should work`() = runTest {
        val entity = createLivingEntity()
        val effect = SimplePotionEffect("speed", 1, 200)

        entity.addPotionEffect(effect)
        assertTrue(entity.hasPotionEffect("speed"))
        assertEquals(1, entity.getPotionEffects().size)

        entity.removePotionEffect("speed")
        assertFalse(entity.hasPotionEffect("speed"))
    }

    @Test
    fun `remove should mark entity as removed`() = runTest {
        val entity = createLivingEntity()
        entity.remove()
        assertTrue(entity.isRemoved)
        assertFalse(entity.isAlive)
    }

    @Test
    fun `passengers should work`() = runTest {
        val mount = createLivingEntity()
        val rider = createLivingEntity()

        mount.addPassenger(rider)
        assertEquals(1, mount.passengers.size)
        assertEquals(mount, rider.vehicle)

        mount.removePassenger(rider)
        assertEquals(0, mount.passengers.size)
        assertNull(rider.vehicle)
    }
}
