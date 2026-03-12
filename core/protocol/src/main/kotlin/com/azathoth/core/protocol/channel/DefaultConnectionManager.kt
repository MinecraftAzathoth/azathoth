package com.azathoth.core.protocol.channel

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.protocol.packet.ClientBoundPacket
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val logger = KotlinLogging.logger {}

/**
 * 默认连接管理器实现
 */
class DefaultConnectionManager : ConnectionManager {

    private val connectionsById = ConcurrentHashMap<String, Connection>()
    private val connectionsByPlayer = ConcurrentHashMap<PlayerId, Connection>()
    private val listeners = CopyOnWriteArrayList<ConnectionListener>()

    override val connectionCount: Int get() = connectionsById.size

    override fun getConnection(connectionId: String): Connection? {
        return connectionsById[connectionId]
    }

    override fun getConnectionByPlayer(playerId: PlayerId): Connection? {
        return connectionsByPlayer[playerId]
    }

    override fun getAllConnections(): Collection<Connection> {
        return connectionsById.values
    }

    override suspend fun broadcast(packet: ClientBoundPacket) {
        for (connection in connectionsById.values) {
            try {
                connection.sendPacket(packet)
            } catch (e: Exception) {
                logger.warn(e) { "广播数据包到连接 ${connection.id} 失败" }
            }
        }
    }

    override suspend fun broadcast(packet: ClientBoundPacket, filter: (Connection) -> Boolean) {
        for (connection in connectionsById.values) {
            if (filter(connection)) {
                try {
                    connection.sendPacket(packet)
                } catch (e: Exception) {
                    logger.warn(e) { "广播数据包到连接 ${connection.id} 失败" }
                }
            }
        }
    }

    override fun addListener(listener: ConnectionListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: ConnectionListener) {
        listeners.remove(listener)
    }

    override suspend fun closeAll(reason: String) {
        logger.info { "关闭所有连接: $reason, 当前连接数: ${connectionsById.size}" }
        for (connection in connectionsById.values) {
            try {
                connection.close(reason)
            } catch (e: Exception) {
                logger.warn(e) { "关闭连接 ${connection.id} 失败" }
            }
        }
        connectionsById.clear()
        connectionsByPlayer.clear()
    }

    /**
     * 添加连接
     */
    suspend fun addConnection(connection: Connection) {
        connectionsById[connection.id] = connection
        connection.playerId?.let { connectionsByPlayer[it] = connection }
        logger.debug { "添加连接: ${connection.id}, 当前连接数: ${connectionsById.size}" }
        for (listener in listeners) {
            try {
                listener.onConnect(connection)
            } catch (e: Exception) {
                logger.warn(e) { "连接监听器 onConnect 异常" }
            }
        }
    }

    /**
     * 移除连接
     */
    suspend fun removeConnection(connectionId: String, reason: String = "") {
        val connection = connectionsById.remove(connectionId) ?: return
        connection.playerId?.let { connectionsByPlayer.remove(it) }
        logger.debug { "移除连接: $connectionId, 原因: $reason, 当前连接数: ${connectionsById.size}" }
        for (listener in listeners) {
            try {
                listener.onDisconnect(connection, reason)
            } catch (e: Exception) {
                logger.warn(e) { "连接监听器 onDisconnect 异常" }
            }
        }
    }

    /**
     * 绑定玩家到连接
     */
    fun bindPlayer(connectionId: String, playerId: PlayerId) {
        val connection = connectionsById[connectionId] ?: return
        connectionsByPlayer[playerId] = connection
        logger.debug { "绑定玩家 $playerId 到连接 $connectionId" }
    }

    /**
     * 解绑玩家
     */
    fun unbindPlayer(playerId: PlayerId) {
        connectionsByPlayer.remove(playerId)
        logger.debug { "解绑玩家 $playerId" }
    }
}
