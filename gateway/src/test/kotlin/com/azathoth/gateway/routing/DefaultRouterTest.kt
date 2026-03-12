package com.azathoth.gateway.routing

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.protocol.channel.Connection
import com.azathoth.core.protocol.channel.ConnectionState
import com.azathoth.core.protocol.packet.ClientBoundPacket
import com.azathoth.core.protocol.packet.ProtocolState
import com.azathoth.gateway.balancer.BalancingStrategy
import com.azathoth.gateway.balancer.DefaultGatewayLoadBalancer
import com.azathoth.gateway.session.DefaultPlayerSession
import com.azathoth.gateway.session.PlayerSession
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultRouterTest {

    private lateinit var registry: DefaultInstanceRegistry
    private lateinit var router: DefaultRouter

    private fun testSession(): PlayerSession = DefaultPlayerSession(
        playerId = PlayerId.of("player-1"),
        playerName = "Steve",
        connection = mockConnection()
    )

    private fun mockConnection(): Connection = object : Connection {
        override val id = "conn-1"
        override val playerId: PlayerId? = null
        override val state = ConnectionState.CONNECTED
        override val protocolState = ProtocolState.PLAY
        override val protocolVersion = 767
        override val remoteAddress = "127.0.0.1"
        override val localAddress = "0.0.0.0"
        override val connectTime = System.currentTimeMillis()
        override val lastActiveTime = System.currentTimeMillis()
        override val isConnected = true
        override suspend fun sendPacket(packet: ClientBoundPacket) {}
        override suspend fun sendPackets(packets: List<ClientBoundPacket>) {}
        override suspend fun flush() {}
        override suspend fun close(reason: String) {}
        override suspend fun disconnect(reason: String) {}
        override fun setAttribute(key: String, value: Any?) {}
        override fun <T> getAttribute(key: String): T? = null
        override fun removeAttribute(key: String) {}
    }

    @BeforeEach
    fun setup() {
        registry = DefaultInstanceRegistry()
        router = DefaultRouter(registry, DefaultGatewayLoadBalancer(BalancingStrategy.ROUND_ROBIN))
    }

    @Test
    fun `route to specific instance by id`() = runTest {
        val instance = SimpleGameInstance(
            instanceId = InstanceId.of("lobby-1"),
            type = InstanceType.LOBBY,
            name = "Lobby 1",
            host = "localhost",
            port = 25566
        )
        registry.register(instance)

        val result = router.route(SimpleRouteRequest(
            session = testSession(),
            targetInstanceId = InstanceId.of("lobby-1")
        ))
        assertTrue(result.success)
        assertEquals("lobby-1", result.targetInstance?.instanceId?.value)
    }

    @Test
    fun `route fails for nonexistent instance id`() = runTest {
        val result = router.route(SimpleRouteRequest(
            session = testSession(),
            targetInstanceId = InstanceId.of("nonexistent")
        ))
        assertFalse(result.success)
        assertNotNull(result.error)
    }

    @Test
    fun `route by type uses load balancer`() = runTest {
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-1"), type = InstanceType.LOBBY,
            name = "Lobby 1", host = "localhost", port = 25566
        ))
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-2"), type = InstanceType.LOBBY,
            name = "Lobby 2", host = "localhost", port = 25567
        ))

        val result = router.route(SimpleRouteRequest(
            session = testSession(),
            targetType = InstanceType.LOBBY
        ))
        assertTrue(result.success)
        assertNotNull(result.targetInstance)
        assertEquals(InstanceType.LOBBY, result.targetInstance?.type)
    }

    @Test
    fun `route fails when no instances available`() = runTest {
        val result = router.route(SimpleRouteRequest(
            session = testSession(),
            targetType = InstanceType.LOBBY
        ))
        assertFalse(result.success)
    }

    @Test
    fun `route skips full instances`() = runTest {
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-1"), type = InstanceType.LOBBY,
            name = "Lobby 1", host = "localhost", port = 25566,
            currentPlayers = 100, maxPlayers = 100
        ))
        val result = router.route(SimpleRouteRequest(
            session = testSession(),
            targetType = InstanceType.LOBBY
        ))
        assertFalse(result.success)
    }

    @Test
    fun `route rule matches by type`() = runTest {
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-1"), type = InstanceType.LOBBY,
            name = "Lobby 1", host = "localhost", port = 25566, currentPlayers = 50
        ))
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-2"), type = InstanceType.LOBBY,
            name = "Lobby 2", host = "localhost", port = 25567, currentPlayers = 10
        ))

        router.addRule(DefaultRouteRule("lobby-rule", priority = 10, targetType = InstanceType.LOBBY))

        val result = router.route(SimpleRouteRequest(
            session = testSession(),
            targetType = InstanceType.LOBBY
        ))
        assertTrue(result.success)
        // DefaultRouteRule picks the one with fewest players
        assertEquals("lobby-2", result.targetInstance?.instanceId?.value)
    }

    @Test
    fun `get available instances filters correctly`() = runTest {
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-1"), type = InstanceType.LOBBY,
            name = "Lobby 1", host = "localhost", port = 25566
        ))
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("world-1"), type = InstanceType.WORLD,
            name = "World 1", host = "localhost", port = 25568
        ))

        val lobbies = router.getAvailableInstances(InstanceType.LOBBY)
        assertEquals(1, lobbies.size)
        assertEquals(InstanceType.LOBBY, lobbies[0].type)
    }

    @Test
    fun `instance registry heartbeat updates player count`() = runTest {
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-1"), type = InstanceType.LOBBY,
            name = "Lobby 1", host = "localhost", port = 25566, currentPlayers = 0
        ))
        registry.heartbeat(InstanceId.of("lobby-1"), 42)
        val instance = registry.getInstance(InstanceId.of("lobby-1"))
        assertEquals(42, instance?.currentPlayers)
    }

    @Test
    fun `instance registry unregister removes instance`() = runTest {
        registry.register(SimpleGameInstance(
            instanceId = InstanceId.of("lobby-1"), type = InstanceType.LOBBY,
            name = "Lobby 1", host = "localhost", port = 25566
        ))
        registry.unregister(InstanceId.of("lobby-1"))
        assertNull(registry.getInstance(InstanceId.of("lobby-1")))
    }
}
