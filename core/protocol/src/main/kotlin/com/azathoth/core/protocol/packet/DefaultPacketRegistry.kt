package com.azathoth.core.protocol.packet

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * 默认数据包注册表实现
 */
class DefaultPacketRegistry : PacketRegistry {

    /** 复合键: (state, direction, packetId) -> packetClass */
    private val idToClass = ConcurrentHashMap<PacketKey, Class<out Packet>>()

    /** 反向映射: (state, direction, packetClass) -> packetId */
    private val classToId = ConcurrentHashMap<ClassKey, Int>()

    override fun <T : Packet> register(
        state: ProtocolState,
        direction: PacketDirection,
        packetId: Int,
        packetClass: Class<T>
    ) {
        val key = PacketKey(state, direction, packetId)
        val classKey = ClassKey(state, direction, packetClass)
        idToClass[key] = packetClass
        classToId[classKey] = packetId
        logger.debug { "注册数据包: state=$state, direction=$direction, id=0x${packetId.toString(16)}, class=${packetClass.simpleName}" }
    }

    override fun getPacketClass(
        state: ProtocolState,
        direction: PacketDirection,
        packetId: Int
    ): Class<out Packet>? {
        return idToClass[PacketKey(state, direction, packetId)]
    }

    override fun <T : Packet> getPacketId(
        state: ProtocolState,
        direction: PacketDirection,
        packetClass: Class<T>
    ): Int? {
        return classToId[ClassKey(state, direction, packetClass)]
    }

    private data class PacketKey(
        val state: ProtocolState,
        val direction: PacketDirection,
        val packetId: Int
    )

    private data class ClassKey(
        val state: ProtocolState,
        val direction: PacketDirection,
        val packetClass: Class<out Packet>
    )
}
