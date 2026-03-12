package com.azathoth.game.engine.world

import com.azathoth.core.common.identity.WorldId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultWorldManagerTest {
    private lateinit var manager: DefaultWorldManager

    @BeforeEach
    fun setup() {
        manager = DefaultWorldManager()
    }

    @Test
    fun `createWorld should create and register world`() = runTest {
        val world = manager.createWorld("test-world", WorldType.OVERWORLD)

        assertNotNull(world)
        assertEquals("test-world", world.name)
        assertEquals(WorldType.OVERWORLD, world.type)
        assertTrue(world.isLoaded)
    }

    @Test
    fun `getWorld should return created world`() = runTest {
        val world = manager.createWorld("test-world", WorldType.OVERWORLD)

        val found = manager.getWorld(world.worldId)
        assertNotNull(found)
        assertEquals(world.worldId, found!!.worldId)
    }

    @Test
    fun `getWorldByName should return correct world`() = runTest {
        manager.createWorld("world-a", WorldType.OVERWORLD)
        manager.createWorld("world-b", WorldType.INSTANCE)

        val found = manager.getWorldByName("world-b")
        assertNotNull(found)
        assertEquals("world-b", found!!.name)
    }

    @Test
    fun `first created world becomes default`() = runTest {
        val first = manager.createWorld("first", WorldType.OVERWORLD)
        manager.createWorld("second", WorldType.INSTANCE)

        assertEquals(first.worldId, manager.getDefaultWorld().worldId)
    }

    @Test
    fun `unloadWorld should mark world as not loaded`() = runTest {
        val world = manager.createWorld("test", WorldType.OVERWORLD)
        manager.unloadWorld(world.worldId)

        val found = manager.getWorld(world.worldId)
        assertNotNull(found)
        assertFalse(found!!.isLoaded)
    }

    @Test
    fun `loadWorld should mark world as loaded`() = runTest {
        val world = manager.createWorld("test", WorldType.OVERWORLD)
        manager.unloadWorld(world.worldId)
        manager.loadWorld(world.worldId)

        assertTrue(manager.getWorld(world.worldId)!!.isLoaded)
    }

    @Test
    fun `deleteWorld should remove world`() = runTest {
        val world = manager.createWorld("test", WorldType.OVERWORLD)
        manager.deleteWorld(world.worldId)

        assertNull(manager.getWorld(world.worldId))
        assertNull(manager.getWorldByName("test"))
    }

    @Test
    fun `getWorlds should return all worlds`() = runTest {
        manager.createWorld("a", WorldType.OVERWORLD)
        manager.createWorld("b", WorldType.INSTANCE)
        manager.createWorld("c", WorldType.ARENA)

        assertEquals(3, manager.getWorlds().size)
    }

    @Test
    fun `loadChunk and unloadChunk should work`() = runTest {
        val world = manager.createWorld("test", WorldType.OVERWORLD)
        val chunkPos = ChunkPosition(0, 0)

        val chunk = world.loadChunk(chunkPos)
        assertNotNull(chunk)
        assertTrue(world.isChunkLoaded(chunkPos))
        assertEquals(1, world.loadedChunkCount)

        world.unloadChunk(chunkPos)
        assertFalse(world.isChunkLoaded(chunkPos))
        assertEquals(0, world.loadedChunkCount)
    }
}
