package com.azathoth.core.protocol.channel

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.protocol.packet.ClientBoundPacket
import com.azathoth.core.protocol.packet.ProtocolState

/**
 * 连接状态
 */
enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    AUTHENTICATED,
    DISCONNECTING,
    DISCONNECTED
}

/**
 * 网络连接接口
 */
interface Connection {
    /** 连接ID */
    val id: String
    
    /** 关联的玩家ID */
    val playerId: PlayerId?
    
    /** 连接状态 */
    val state: ConnectionState
    
    /** 协议状态 */
    val protocolState: ProtocolState
    
    /** 协议版本 */
    val protocolVersion: Int
    
    /** 远程地址 */
    val remoteAddress: String
    
    /** 本地地址 */
    val localAddress: String
    
    /** 连接时间戳 */
    val connectTime: Long
    
    /** 最后活动时间 */
    val lastActiveTime: Long
    
    /** 是否已连接 */
    val isConnected: Boolean
    
    /** 发送数据包 */
    suspend fun sendPacket(packet: ClientBoundPacket)
    
    /** 批量发送数据包 */
    suspend fun sendPackets(packets: List<ClientBoundPacket>)
    
    /** 刷新发送缓冲区 */
    suspend fun flush()
    
    /** 关闭连接 */
    suspend fun close(reason: String = "")
    
    /** 强制断开连接 */
    suspend fun disconnect(reason: String = "")
    
    /** 设置属性 */
    fun setAttribute(key: String, value: Any?)
    
    /** 获取属性 */
    fun <T> getAttribute(key: String): T?
    
    /** 移除属性 */
    fun removeAttribute(key: String)
}

/**
 * 连接监听器
 */
interface ConnectionListener {
    /** 连接建立 */
    suspend fun onConnect(connection: Connection)
    
    /** 连接断开 */
    suspend fun onDisconnect(connection: Connection, reason: String)
    
    /** 连接状态变更 */
    suspend fun onStateChange(connection: Connection, oldState: ConnectionState, newState: ConnectionState)
    
    /** 连接异常 */
    suspend fun onException(connection: Connection, cause: Throwable)
}

/**
 * 连接管理器
 */
interface ConnectionManager {
    /** 所有连接数 */
    val connectionCount: Int
    
    /** 获取连接 */
    fun getConnection(connectionId: String): Connection?
    
    /** 获取玩家连接 */
    fun getConnectionByPlayer(playerId: PlayerId): Connection?
    
    /** 获取所有连接 */
    fun getAllConnections(): Collection<Connection>
    
    /** 广播数据包给所有连接 */
    suspend fun broadcast(packet: ClientBoundPacket)
    
    /** 广播数据包给指定连接 */
    suspend fun broadcast(packet: ClientBoundPacket, filter: (Connection) -> Boolean)
    
    /** 添加连接监听器 */
    fun addListener(listener: ConnectionListener)
    
    /** 移除连接监听器 */
    fun removeListener(listener: ConnectionListener)
    
    /** 关闭所有连接 */
    suspend fun closeAll(reason: String = "")
}
