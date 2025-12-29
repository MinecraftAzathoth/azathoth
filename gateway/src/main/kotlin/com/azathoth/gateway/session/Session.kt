package com.azathoth.gateway.session

import com.azathoth.core.common.identity.GatewayId
import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.protocol.channel.Connection

/**
 * 会话状态
 */
enum class SessionState {
    /** 初始化 */
    INITIALIZING,
    /** 认证中 */
    AUTHENTICATING,
    /** 已认证 */
    AUTHENTICATED,
    /** 连接游戏实例中 */
    CONNECTING_INSTANCE,
    /** 游戏中 */
    PLAYING,
    /** 传送中 */
    TRANSFERRING,
    /** 断开连接中 */
    DISCONNECTING,
    /** 已断开 */
    DISCONNECTED
}

/**
 * 玩家会话
 */
interface PlayerSession {
    /** 会话ID */
    val sessionId: String
    
    /** 玩家ID */
    val playerId: PlayerId
    
    /** 玩家名称 */
    val playerName: String
    
    /** 会话状态 */
    val state: SessionState
    
    /** 关联的网关ID */
    val gatewayId: GatewayId
    
    /** 当前游戏实例ID */
    val currentInstanceId: InstanceId?
    
    /** 网络连接 */
    val connection: Connection
    
    /** 会话创建时间 */
    val createdAt: Long
    
    /** 最后活动时间 */
    val lastActiveTime: Long
    
    /** 协议版本 */
    val protocolVersion: Int
    
    /** 客户端品牌 */
    val clientBrand: String?
    
    /** 会话属性 */
    fun getAttribute(key: String): Any?
    
    /** 设置会话属性 */
    fun setAttribute(key: String, value: Any?)
    
    /** 移除会话属性 */
    fun removeAttribute(key: String)
    
    /** 更新最后活动时间 */
    fun touch()
}

/**
 * 会话管理器
 */
interface SessionManager {
    /** 会话总数 */
    val sessionCount: Int
    
    /** 创建会话 */
    suspend fun createSession(
        playerId: PlayerId,
        playerName: String,
        connection: Connection
    ): PlayerSession
    
    /** 获取会话 */
    fun getSession(sessionId: String): PlayerSession?
    
    /** 通过玩家ID获取会话 */
    fun getSessionByPlayer(playerId: PlayerId): PlayerSession?
    
    /** 获取所有会话 */
    fun getAllSessions(): Collection<PlayerSession>
    
    /** 获取指定实例的会话 */
    fun getSessionsByInstance(instanceId: InstanceId): Collection<PlayerSession>
    
    /** 移除会话 */
    suspend fun removeSession(sessionId: String): PlayerSession?
    
    /** 更新会话状态 */
    suspend fun updateState(sessionId: String, state: SessionState)
    
    /** 添加会话监听器 */
    fun addListener(listener: SessionListener)
    
    /** 移除会话监听器 */
    fun removeListener(listener: SessionListener)
}

/**
 * 会话监听器
 */
interface SessionListener {
    /** 会话创建 */
    suspend fun onSessionCreated(session: PlayerSession)
    
    /** 会话销毁 */
    suspend fun onSessionDestroyed(session: PlayerSession)
    
    /** 会话状态变更 */
    suspend fun onStateChanged(
        session: PlayerSession,
        oldState: SessionState,
        newState: SessionState
    )
}

/**
 * 分布式会话存储
 */
interface SessionStore {
    /** 保存会话 */
    suspend fun save(session: PlayerSession)
    
    /** 加载会话 */
    suspend fun load(sessionId: String): PlayerSession?
    
    /** 删除会话 */
    suspend fun delete(sessionId: String)
    
    /** 刷新会话过期时间 */
    suspend fun refresh(sessionId: String, ttlSeconds: Long)
    
    /** 检查会话是否存在 */
    suspend fun exists(sessionId: String): Boolean
}
