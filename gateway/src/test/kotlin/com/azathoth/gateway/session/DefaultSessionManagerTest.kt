package com.azathoth.gateway.session

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.protocol.channel.Connection
import com.azathoth.core.protocol.channel.ConnectionState
import com.azathoth.core.protocol.packet.ClientBoundPacket
import com.azathoth.core.protocol.packet.ProtocolState
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultSessionManagerTest {

    private lateinit var manager: DefaultSessionManager

    private fun mockConnection(id: String = "conn-1"): Connection = object : Connection {
        override val id: String = id
        override val playerId: PlayerId? = null
        override val state: ConnectionState = ConnectionState.CONNECTED
        override val protocolState: ProtocolState = ProtocolState.PLAY
        override val protocolVersion: Int = 767
        override val remoteAddress: String = "127.0.0.1"
        override val localAddress: String = "0.0.0.0"
        override val connectTime: Long = System.currentTimeMillis()
        override val lastActiveTime: Long = System.currentTimeMillis()
        override val isConnected: Boolean = true
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
        manager = DefaultSessionManager()
    }

    @Test
    fun `create session returns valid session`() = runTest {
        val playerId = PlayerId.of("player-1")
        val session = manager.createSession(playerId, "Steve", mockConnection())

        assertNotNull(session.sessionId)
        assertEquals(playerId, session.playerId)
        assertEquals("Steve", session.playerName)
        assertEquals(SessionState.INITIALIZING, session.state)
        assertEquals(1, manager.sessionCount)
    }

    @Test
    fun `get session by id`() = runTest {
        val session = manager.createSession(PlayerId.of("p1"), "Steve", mockConnection())
        val found = manager.getSession(session.sessionId)
        assertEquals(session, found)
    }

    @Test
    fun `get session by player id`() = runTest {
        val playerId = PlayerId.of("p1")
        val session = manager.createSession(playerId, "Steve", mockConnection())
        val found = manager.getSessionByPlayer(playerId)
        assertEquals(session, found)
    }

    @Test
    fun `get session returns null for unknown id`() {
        assertNull(manager.getSession("nonexistent"))
    }

    @Test
    fun `remove session`() = runTest {
        val session = manager.createSession(PlayerId.of("p1"), "Steve", mockConnection())
        val removed = manager.removeSession(session.sessionId)
        assertEquals(session, removed)
        assertEquals(0, manager.sessionCount)
        assertNull(manager.getSession(session.sessionId))
        assertNull(manager.getSessionByPlayer(PlayerId.of("p1")))
    }

    @Test
    fun `remove nonexistent session returns null`() = runTest {
        assertNull(manager.removeSession("nonexistent"))
    }

    @Test
    fun `update session state`() = runTest {
        val session = manager.createSession(PlayerId.of("p1"), "Steve", mockConnection())
        manager.updateState(session.sessionId, SessionState.AUTHENTICATED)
        assertEquals(SessionState.AUTHENTICATED, manager.getSession(session.sessionId)?.state)
    }

    @Test
    fun `get all sessions`() = runTest {
        manager.createSession(PlayerId.of("p1"), "Steve", mockConnection("c1"))
        manager.createSession(PlayerId.of("p2"), "Alex", mockConnection("c2"))
        assertEquals(2, manager.getAllSessions().size)
    }

    @Test
    fun `session attributes work correctly`() = runTest {
        val session = manager.createSession(PlayerId.of("p1"), "Steve", mockConnection())
        session.setAttribute("key1", "value1")
        assertEquals("value1", session.getAttribute("key1"))
        session.removeAttribute("key1")
        assertNull(session.getAttribute("key1"))
    }

    @Test
    fun `session touch updates last active time`() = runTest {
        val session = manager.createSession(PlayerId.of("p1"), "Steve", mockConnection())
        val before = session.lastActiveTime
        Thread.sleep(10)
        session.touch()
        assertTrue(session.lastActiveTime >= before)
    }

    @Test
    fun `listener receives session events`() = runTest {
        var created = false
        var destroyed = false
        var stateChanged = false

        manager.addListener(object : SessionListener {
            override suspend fun onSessionCreated(session: PlayerSession) { created = true }
            override suspend fun onSessionDestroyed(session: PlayerSession) { destroyed = true }
            override suspend fun onStateChanged(session: PlayerSession, oldState: SessionState, newState: SessionState) {
                stateChanged = true
            }
        })

        val session = manager.createSession(PlayerId.of("p1"), "Steve", mockConnection())
        assertTrue(created)

        manager.updateState(session.sessionId, SessionState.PLAYING)
        assertTrue(stateChanged)

        manager.removeSession(session.sessionId)
        assertTrue(destroyed)
    }
}

class InMemorySessionStoreTest {

    @Test
    fun `save and load session`() = runTest {
        val store = InMemorySessionStore()
        val session = createTestSession()
        store.save(session)
        assertEquals(session, store.load(session.sessionId))
    }

    @Test
    fun `delete session`() = runTest {
        val store = InMemorySessionStore()
        val session = createTestSession()
        store.save(session)
        store.delete(session.sessionId)
        assertNull(store.load(session.sessionId))
        assertFalse(store.exists(session.sessionId))
    }

    @Test
    fun `exists returns correct value`() = runTest {
        val store = InMemorySessionStore()
        val session = createTestSession()
        assertFalse(store.exists(session.sessionId))
        store.save(session)
        assertTrue(store.exists(session.sessionId))
    }

    private fun createTestSession(): PlayerSession {
        val conn = object : Connection {
            override val id = "test-conn"
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
        return DefaultPlayerSession(
            playerId = PlayerId.of("test-player"),
            playerName = "TestPlayer",
            connection = conn
        )
    }
}
