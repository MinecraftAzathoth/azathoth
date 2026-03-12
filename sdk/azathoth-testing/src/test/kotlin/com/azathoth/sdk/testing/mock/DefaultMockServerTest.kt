package com.azathoth.sdk.testing.mock

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class DefaultMockServerTest {

    private lateinit var server: MockServer

    @BeforeEach
    fun setup() {
        server = MockServerFactory.builder()
            .name("test-server")
            .tps(20)
            .withDefaultWorld("world")
            .build()
    }

    // --- MockServer tests ---

    @Test
    fun `server starts and stops correctly`() = runTest {
        assertFalse(server.isRunning)

        server.start()
        assertTrue(server.isRunning)
        assertEquals("test-server", server.name)

        server.stop()
        assertFalse(server.isRunning)
    }

    @Test
    fun `server creates default world on start`() = runTest {
        server.start()
        assertNotNull(server.getWorld("world"))
        assertEquals(1, server.getWorlds().size)
    }

    @Test
    fun `advanceTicks increments tick counter`() = runTest {
        server.start()
        assertEquals(0, server.currentTick)

        server.advanceTicks(100)
        assertEquals(100, server.currentTick)

        server.advanceTicks(50)
        assertEquals(150, server.currentTick)
    }

    @Test
    fun `createDefault produces a working server`() = runTest {
        val defaultServer = MockServerFactory.createDefault()
        defaultServer.start()
        assertTrue(defaultServer.isRunning)
        assertNotNull(defaultServer.getWorld("world"))
        defaultServer.stop()
    }

    @Test
    fun `addPlugin stores plugin`() = runTest {
        server.start()
        server.addPlugin("dummy-plugin")
        // 不抛异常即可
    }

    // --- MockPlayer tests ---

    @Test
    fun `createPlayer adds player to server`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        assertEquals("Steve", player.name)
        assertTrue(player.isOnline)
        assertEquals(1, server.getPlayers().size)
    }

    @Test
    fun `createPlayer with UUID`() = runTest {
        server.start()
        val uuid = UUID.randomUUID()
        val player = server.createPlayer(uuid, "Alex")

        assertEquals(uuid, player.playerId)
        assertEquals("Alex", player.name)
    }

    @Test
    fun `removePlayer removes from server`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")
        assertEquals(1, server.getPlayers().size)

        server.removePlayer(player.playerId)
        assertEquals(0, server.getPlayers().size)
    }

    @Test
    fun `player chat records sent messages`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        player.chat("Hello")
        player.chat("World")

        assertEquals(listOf("Hello", "World"), player.sentMessages)
    }

    @Test
    fun `player sendMessage records received messages`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        player.sendMessage("Welcome!")
        player.sendMessage("Have fun!")

        assertEquals(2, player.receivedMessages.size)
        player.assertReceivedMessage("Welcome!")
        player.assertReceivedMessageContaining("fun")
    }

    @Test
    fun `player assertReceivedMessage fails when message not received`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        assertThrows(AssertionError::class.java) {
            player.assertReceivedMessage("not sent")
        }
    }

    @Test
    fun `player performCommand records commands`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        val result = player.performCommand("help")
        assertTrue(result)
        assertEquals(listOf("help"), player.executedCommands)
    }

    @Test
    fun `player moveTo updates position`() = runTest {
        server.start()
        val player = server.createPlayer("Steve") as DefaultMockPlayer

        player.moveTo(10.0, 65.0, -20.0)
        assertEquals(10.0, player.x)
        assertEquals(65.0, player.y)
        assertEquals(-20.0, player.z)
    }

    @Test
    fun `player clearMessages clears all records`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        player.chat("msg")
        player.sendMessage("recv")
        player.performCommand("cmd")

        player.clearMessages()
        assertTrue(player.sentMessages.isEmpty())
        assertTrue(player.receivedMessages.isEmpty())
        assertTrue(player.executedCommands.isEmpty())
    }

    @Test
    fun `player permissions work`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        assertFalse(player.hasPermission("admin.kick"))
        player.permissions.add("admin.kick")
        assertTrue(player.hasPermission("admin.kick"))
    }

    @Test
    fun `player health defaults`() = runTest {
        server.start()
        val player = server.createPlayer("Steve")

        assertEquals(20.0, player.health)
        assertEquals(20.0, player.maxHealth)

        player.health = 10.0
        assertEquals(10.0, player.health)
    }

    // --- MockWorld tests ---

    @Test
    fun `createWorld adds world to server`() = runTest {
        server.start()
        val nether = server.createWorld("nether")

        assertEquals("nether", nether.name)
        assertTrue(nether.isLoaded)
        assertNotNull(server.getWorld("nether"))
        assertEquals(2, server.getWorlds().size) // "world" + "nether"
    }

    @Test
    fun `world block storage works`() = runTest {
        server.start()
        val world = server.getWorld("world")!!

        assertEquals("air", world.getBlockType(0, 64, 0))

        world.setBlock(0, 64, 0, "stone")
        assertEquals("stone", world.getBlockType(0, 64, 0))

        world.setBlock(1, 65, 2, "dirt")
        assertEquals("dirt", world.getBlockType(1, 65, 2))
    }

    @Test
    fun `world entity management works`() = runTest {
        server.start()
        val world = server.getWorld("world")!!

        assertEquals(0, world.getEntityCount())

        world.spawnMockEntity("zombie", 10.0, 64.0, 10.0)
        world.spawnMockEntity("skeleton", 20.0, 64.0, 20.0)
        assertEquals(2, world.getEntityCount())

        world.clearEntities()
        assertEquals(0, world.getEntityCount())
    }

    @Test
    fun `spawnMockEntity returns MockEntity`() = runTest {
        server.start()
        val world = server.getWorld("world")!!

        val entity = world.spawnMockEntity("creeper", 5.0, 64.0, 5.0)
        assertTrue(entity is MockEntity)
        assertEquals("creeper", (entity as MockEntity).type)
    }
}
