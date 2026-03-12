package com.azathoth.gateway.session

import com.azathoth.core.common.identity.GatewayId
import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.protocol.channel.Connection
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

// --- DefaultPlayerSession ---

class DefaultPlayerSession(
    override val sessionId: String = UUID.randomUUID().toString(),
    override val playerId: PlayerId,
    override val playerName: String,
    override val connection: Connection,
    override val gatewayId: GatewayId = GatewayId.generate(),
    override val protocolVersion: Int = connection.protocolVersion,
    override val clientBrand: String? = null,
    override val createdAt: Long = System.currentTimeMillis()
) : PlayerSession {

    @Volatile
    override var state: SessionState = SessionState.INITIALIZING
        internal set

    @Volatile
    override var currentInstanceId: InstanceId? = null
        internal set

    @Volatile
    override var lastActiveTime: Long = System.currentTimeMillis()
        private set

    private val attributes = ConcurrentHashMap<String, Any?>()

    override fun getAttribute(key: String): Any? = attributes[key]

    override fun setAttribute(key: String, value: Any?) {
        if (value == null) attributes.remove(key) else attributes[key] = value
    }

    override fun removeAttribute(key: String) {
        attributes.remove(key)
    }

    override fun touch() {
        lastActiveTime = System.currentTimeMillis()
    }
}

// --- DefaultSessionManager ---

class DefaultSessionManager(
    private val gatewayId: GatewayId = GatewayId.generate()
) : SessionManager {

    private val sessions = ConcurrentHashMap<String, DefaultPlayerSession>()
    private val playerIndex = ConcurrentHashMap<PlayerId, String>()
    private val listeners = ConcurrentHashMap.newKeySet<SessionListener>()

    override val sessionCount: Int get() = sessions.size

    override suspend fun createSession(
        playerId: PlayerId,
        playerName: String,
        connection: Connection
    ): PlayerSession {
        val session = DefaultPlayerSession(
            playerId = playerId,
            playerName = playerName,
            connection = connection,
            gatewayId = gatewayId
        )
        sessions[session.sessionId] = session
        playerIndex[playerId] = session.sessionId
        logger.info { "会话已创建: ${session.sessionId} (玩家: $playerName)" }
        listeners.forEach { it.onSessionCreated(session) }
        return session
    }

    override fun getSession(sessionId: String): PlayerSession? = sessions[sessionId]

    override fun getSessionByPlayer(playerId: PlayerId): PlayerSession? =
        playerIndex[playerId]?.let { sessions[it] }

    override fun getAllSessions(): Collection<PlayerSession> = sessions.values

    override fun getSessionsByInstance(instanceId: InstanceId): Collection<PlayerSession> =
        sessions.values.filter { it.currentInstanceId == instanceId }

    override suspend fun removeSession(sessionId: String): PlayerSession? {
        val session = sessions.remove(sessionId) ?: return null
        playerIndex.remove(session.playerId)
        logger.info { "会话已移除: $sessionId (玩家: ${session.playerName})" }
        listeners.forEach { it.onSessionDestroyed(session) }
        return session
    }

    override suspend fun updateState(sessionId: String, state: SessionState) {
        val session = sessions[sessionId] ?: return
        val oldState = session.state
        session.state = state
        logger.debug { "会话状态变更: $sessionId $oldState -> $state" }
        listeners.forEach { it.onStateChanged(session, oldState, state) }
    }

    override fun addListener(listener: SessionListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: SessionListener) {
        listeners.remove(listener)
    }
}

// --- InMemorySessionStore ---

class InMemorySessionStore : SessionStore {
    private val store = ConcurrentHashMap<String, PlayerSession>()
    private val expiry = ConcurrentHashMap<String, Long>()

    override suspend fun save(session: PlayerSession) {
        store[session.sessionId] = session
    }

    override suspend fun load(sessionId: String): PlayerSession? {
        val exp = expiry[sessionId]
        if (exp != null && System.currentTimeMillis() > exp) {
            delete(sessionId)
            return null
        }
        return store[sessionId]
    }

    override suspend fun delete(sessionId: String) {
        store.remove(sessionId)
        expiry.remove(sessionId)
    }

    override suspend fun refresh(sessionId: String, ttlSeconds: Long) {
        if (store.containsKey(sessionId)) {
            expiry[sessionId] = System.currentTimeMillis() + ttlSeconds * 1000
        }
    }

    override suspend fun exists(sessionId: String): Boolean {
        val exp = expiry[sessionId]
        if (exp != null && System.currentTimeMillis() > exp) {
            delete(sessionId)
            return false
        }
        return store.containsKey(sessionId)
    }
}
