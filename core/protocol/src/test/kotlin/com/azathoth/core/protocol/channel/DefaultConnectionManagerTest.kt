package com.azathoth.core.protocol.channel

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.protocol.packet.ClientBoundPacket
import com.azathoth.core.protocol.packet.PacketDirection
import com.azathoth.core.protocol.packet.ProtocolState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultConnectionManagerTest {

    private lateinit var manager: DefaultConnectionManager

    /** 测试用 Connection 桩 */
    private class StubConnection(
        override val id: String,
        override val playerId: PlayerId? = null,
    ) : Connection {
        override val state: ConnectionState = ConnectionState.CONNECTED
        override val protocolState: ProtocolState = ProtocolState.PLAY
        override val protocolVersion: Int = 767
        override val remoteAddress: String = "127.0.0.1"
        override val localAddress: String = "0.0.0.0"
        override val connectTime: Long = System.currentTimeMillis()
        override val lastActiveTime: Long = System.currentTimeMillis()
        override val isConnected: Boolean = true

        val sentPackets = mutableListOf<ClientBoundPacket>()
        var closed = false

        override suspend fun sendPacket(packet: ClientBoundPacket) { sentPackets.add(packet) }
        override suspend fun sendPackets(packets: List<ClientBoundPacket>) { sentPackets.addAll(packets) }
        override suspend fun flush() {}
        override suspend fun close(reason: String) { closed = true }
        override suspend fun disconnect(reason: String) { closed = true }
        override fun setAttribute(key: String, value: Any?) {}
        override fun <T> getAttribute(key: String): T? = null
        override fun removeAttribute(key: String) {}
    }

    private data class TestClientPacket(
        override val packetId: Int = 0x01,
        override val state: ProtocolState = ProtocolState.PLAY,
        val message: String = "hello"
    ) : ClientBoundPacket

    @BeforeEach
    fun setup() {
        manager = DefaultConnectionManager()
    }

    @Test
    fun `test add and get connection`() = runTest {
        val conn = StubConnection("conn-1")
        manager.addConnection(conn)

        assertEquals(1, manager.connectionCount)
        assertSame(conn, manager.getConnection("conn-1"))
    }

    @Test
    fun `test get connection by player`() = runTest {
        val playerId = PlayerId("player-1")
        val conn = StubConnection("conn-1", playerId)
        manager.addConnection(conn)

        assertSame(conn, manager.getConnectionByPlayer(playerId))
    }

    @Test
    fun `test remove connection`() = runTest {
        val conn = StubConnection("conn-1")
        manager.addConnection(conn)
        manager.removeConnection("conn-1", "test")

        assertEquals(0, manager.connectionCount)
        assertNull(manager.getConnection("conn-1"))
    }

    @Test
    fun `test remove connection also removes player mapping`() = runTest {
        val playerId = PlayerId("player-1")
        val conn = StubConnection("conn-1", playerId)
        manager.addConnection(conn)
        manager.removeConnection("conn-1")

        assertNull(manager.getConnectionByPlayer(playerId))
    }

    @Test
    fun `test broadcast sends to all connections`() = runTest {
        val conn1 = StubConnection("conn-1")
        val conn2 = StubConnection("conn-2")
        manager.addConnection(conn1)
        manager.addConnection(conn2)

        val packet = TestClientPacket()
        manager.broadcast(packet)

        assertEquals(1, conn1.sentPackets.size)
        assertEquals(1, conn2.sentPackets.size)
    }

    @Test
    fun `test broadcast with filter`() = runTest {
        val conn1 = StubConnection("conn-1")
        val conn2 = StubConnection("conn-2")
        manager.addConnection(conn1)
        manager.addConnection(conn2)

        val packet = TestClientPacket()
        manager.broadcast(packet) { it.id == "conn-1" }

        assertEquals(1, conn1.sentPackets.size)
        assertEquals(0, conn2.sentPackets.size)
    }

    @Test
    fun `test getAllConnections`() = runTest {
        val conn1 = StubConnection("conn-1")
        val conn2 = StubConnection("conn-2")
        manager.addConnection(conn1)
        manager.addConnection(conn2)

        val all = manager.getAllConnections()
        assertEquals(2, all.size)
    }

    @Test
    fun `test closeAll closes all connections`() = runTest {
        val conn1 = StubConnection("conn-1")
        val conn2 = StubConnection("conn-2")
        manager.addConnection(conn1)
        manager.addConnection(conn2)

        manager.closeAll("shutdown")

        assertTrue(conn1.closed)
        assertTrue(conn2.closed)
        assertEquals(0, manager.connectionCount)
    }

    @Test
    fun `test listener receives onConnect and onDisconnect`() = runTest {
        val events = mutableListOf<String>()
        manager.addListener(object : ConnectionListener {
            override suspend fun onConnect(connection: Connection) { events.add("connect:${connection.id}") }
            override suspend fun onDisconnect(connection: Connection, reason: String) { events.add("disconnect:${connection.id}:$reason") }
            override suspend fun onStateChange(connection: Connection, oldState: ConnectionState, newState: ConnectionState) {}
            override suspend fun onException(connection: Connection, cause: Throwable) {}
        })

        val conn = StubConnection("conn-1")
        manager.addConnection(conn)
        manager.removeConnection("conn-1", "bye")

        assertEquals(listOf("connect:conn-1", "disconnect:conn-1:bye"), events)
    }

    @Test
    fun `test bindPlayer and unbindPlayer`() = runTest {
        val conn = StubConnection("conn-1")
        manager.addConnection(conn)

        val playerId = PlayerId("player-1")
        manager.bindPlayer("conn-1", playerId)
        assertSame(conn, manager.getConnectionByPlayer(playerId))

        manager.unbindPlayer(playerId)
        assertNull(manager.getConnectionByPlayer(playerId))
    }
}
