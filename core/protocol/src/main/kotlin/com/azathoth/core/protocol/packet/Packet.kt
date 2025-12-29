package com.azathoth.core.protocol.packet

import com.azathoth.core.common.identity.PlayerId

/**
 * 数据包方向
 */
enum class PacketDirection {
    /** 客户端到服务端 */
    SERVERBOUND,
    /** 服务端到客户端 */
    CLIENTBOUND,
    /** 双向 */
    BIDIRECTIONAL
}

/**
 * 数据包协议状态
 */
enum class ProtocolState {
    /** 握手阶段 */
    HANDSHAKE,
    /** 状态查询 */
    STATUS,
    /** 登录阶段 */
    LOGIN,
    /** 配置阶段 */
    CONFIGURATION,
    /** 游戏阶段 */
    PLAY
}

/**
 * 数据包基础接口
 */
interface Packet {
    /** 数据包ID */
    val packetId: Int
    
    /** 数据包方向 */
    val direction: PacketDirection
    
    /** 协议状态 */
    val state: ProtocolState
}

/**
 * 可序列化数据包
 */
interface SerializablePacket : Packet {
    /** 序列化为字节数组 */
    fun serialize(): ByteArray
}

/**
 * 客户端发送的数据包
 */
interface ServerBoundPacket : Packet {
    override val direction: PacketDirection get() = PacketDirection.SERVERBOUND
}

/**
 * 服务端发送的数据包
 */
interface ClientBoundPacket : Packet {
    override val direction: PacketDirection get() = PacketDirection.CLIENTBOUND
}

/**
 * 数据包上下文
 */
interface PacketContext {
    /** 关联的玩家ID */
    val playerId: PlayerId?
    
    /** 连接ID */
    val connectionId: String
    
    /** 协议版本 */
    val protocolVersion: Int
    
    /** 当前协议状态 */
    val state: ProtocolState
    
    /** 远程地址 */
    val remoteAddress: String
    
    /** 发送响应数据包 */
    suspend fun sendPacket(packet: ClientBoundPacket)
    
    /** 关闭连接 */
    suspend fun close(reason: String = "")
}

/**
 * 数据包注册表
 */
interface PacketRegistry {
    /** 注册数据包 */
    fun <T : Packet> register(
        state: ProtocolState,
        direction: PacketDirection,
        packetId: Int,
        packetClass: Class<T>
    )
    
    /** 获取数据包类 */
    fun getPacketClass(
        state: ProtocolState,
        direction: PacketDirection,
        packetId: Int
    ): Class<out Packet>?
    
    /** 获取数据包ID */
    fun <T : Packet> getPacketId(
        state: ProtocolState,
        direction: PacketDirection,
        packetClass: Class<T>
    ): Int?
}
