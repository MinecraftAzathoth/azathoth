package com.azathoth.gateway.transfer

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.core.protocol.channel.Connection
import com.azathoth.core.protocol.channel.ConnectionState
import com.azathoth.core.protocol.packet.ClientBoundPacket
import com.azathoth.core.protocol.packet.ProtocolState
import com.azathoth.gateway.routing.InstanceType
import com.azathoth.gateway.routing.SimpleGameInstance
import com.azathoth.gateway.session.DefaultPlayerSession
import com.azathoth.gateway.session.PlayerSession
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultTransferManagerTest {

    private lateinit var transferManager: DefaultTransferManager

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

    private fun testSession(name: String = "Steve"): PlayerSession = DefaultPlayerSession(
        playerId = PlayerId.of("player-$name"),
        playerName = name,
        connection = mockConnection()
    )

    private fun targetInstance(accepting: Boolean = true, full: Boolean = false) = SimpleGameInstance(
        instanceId = InstanceId.of("target-1"),
        type = InstanceType.LOBBY,
        name = "Target Lobby",
        host = "localhost",
        port = 25567,
        currentPlayers = if (full) 100 else 10,
        maxPlayers = 100,
        acceptingPlayers = accepting
    )

    @BeforeEach
    fun setup() {
        transferManager = DefaultTransferManager()
    }

    @Test
    fun `simple transfer succeeds`() = runTest {
        val result = transferManager.transfer(testSession(), targetInstance(), "test")
        assertTrue(result.isSuccess)
        val tr = (result as Result.Success).value
        assertTrue(tr.success)
        assertTrue(tr.durationMs >= 0)
    }

    @Test
    fun `transfer fails when target not accepting`() = runTest {
        val result = transferManager.transfer(testSession(), targetInstance(accepting = false), "test")
        assertTrue(result.isFailure)
    }

    @Test
    fun `transfer fails when target is full`() = runTest {
        val result = transferManager.transfer(testSession(), targetInstance(full = true), "test")
        assertTrue(result.isFailure)
    }

    @Test
    fun `transfer with full request object`() = runTest {
        val request = SimpleTransferRequest(
            session = testSession(),
            sourceInstanceId = InstanceId.of("source-1"),
            targetInstance = targetInstance(),
            transferData = SimpleTransferData(reason = "dungeon_enter")
        )
        val result = transferManager.transfer(request)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `transfer state transitions through all states`() = runTest {
        val stateTransitions = mutableListOf<Pair<TransferState?, TransferState>>()

        transferManager.addListener(object : TransferListener {
            override suspend fun onTransferStart(request: TransferRequest) {}
            override suspend fun onStateChange(request: TransferRequest, oldState: TransferState, newState: TransferState) {
                stateTransitions.add(oldState to newState)
            }
            override suspend fun onTransferComplete(request: TransferRequest, result: TransferResult) {}
            override suspend fun onTransferFailed(request: TransferRequest, error: String) {}
        })

        transferManager.transfer(testSession(), targetInstance(), "test")

        // Should have transitions: INIT->PREP, PREP->TRANSFERRING, TRANSFERRING->CONNECTING, CONNECTING->COMPLETED
        assertTrue(stateTransitions.size >= 3)
        assertTrue(stateTransitions.any { it.second == TransferState.PREPARING })
        assertTrue(stateTransitions.any { it.second == TransferState.TRANSFERRING })
        assertTrue(stateTransitions.any { it.second == TransferState.COMPLETED })
    }

    @Test
    fun `batch transfer processes all sessions`() = runTest {
        val sessions = listOf(testSession("Alice"), testSession("Bob"), testSession("Charlie"))
        val results = transferManager.transferBatch(sessions, targetInstance(), "batch")
        assertEquals(3, results.size)
        results.values.forEach { assertTrue(it.isSuccess) }
    }

    @Test
    fun `listener receives start and complete events`() = runTest {
        var started = false
        var completed = false

        transferManager.addListener(object : TransferListener {
            override suspend fun onTransferStart(request: TransferRequest) { started = true }
            override suspend fun onStateChange(request: TransferRequest, oldState: TransferState, newState: TransferState) {}
            override suspend fun onTransferComplete(request: TransferRequest, result: TransferResult) { completed = true }
            override suspend fun onTransferFailed(request: TransferRequest, error: String) {}
        })

        transferManager.transfer(testSession(), targetInstance(), "test")
        assertTrue(started)
        assertTrue(completed)
    }

    @Test
    fun `listener receives failure event`() = runTest {
        var failed = false

        transferManager.addListener(object : TransferListener {
            override suspend fun onTransferStart(request: TransferRequest) {}
            override suspend fun onStateChange(request: TransferRequest, oldState: TransferState, newState: TransferState) {}
            override suspend fun onTransferComplete(request: TransferRequest, result: TransferResult) {}
            override suspend fun onTransferFailed(request: TransferRequest, error: String) { failed = true }
        })

        transferManager.transfer(testSession(), targetInstance(accepting = false), "test")
        assertTrue(failed)
    }

    @Test
    fun `completed transfer is removed from pending`() = runTest {
        transferManager.transfer(testSession(), targetInstance(), "test")
        assertTrue(transferManager.getPendingTransfers().isEmpty())
    }

    @Test
    fun `remove listener stops notifications`() = runTest {
        var count = 0
        val listener = object : TransferListener {
            override suspend fun onTransferStart(request: TransferRequest) { count++ }
            override suspend fun onStateChange(request: TransferRequest, oldState: TransferState, newState: TransferState) {}
            override suspend fun onTransferComplete(request: TransferRequest, result: TransferResult) {}
            override suspend fun onTransferFailed(request: TransferRequest, error: String) {}
        }

        transferManager.addListener(listener)
        transferManager.transfer(testSession(), targetInstance(), "test")
        assertEquals(1, count)

        transferManager.removeListener(listener)
        transferManager.transfer(testSession("Bob"), targetInstance(), "test")
        assertEquals(1, count)
    }
}
