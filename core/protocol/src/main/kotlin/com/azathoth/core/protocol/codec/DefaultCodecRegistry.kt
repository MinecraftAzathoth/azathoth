package com.azathoth.core.protocol.codec

import com.azathoth.core.protocol.packet.Packet
import com.azathoth.core.protocol.packet.ProtocolState
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * 默认编解码器注册表实现
 */
class DefaultCodecRegistry : CodecRegistry {

    /** (state, packetClass) -> codec */
    private val codecByClass = ConcurrentHashMap<CodecKey, PacketCodec<out Packet>>()

    /** (state, packetId) -> codec — 需要 codec 对应的 packet 实例来获取 packetId，
     *  所以这里通过注册时额外记录 */
    private val codecById = ConcurrentHashMap<IdKey, PacketCodec<out Packet>>()

    override fun <T : Packet> register(state: ProtocolState, codec: PacketCodec<T>) {
        codecByClass[CodecKey(state, codec.packetClass)] = codec
        logger.debug { "注册编解码器: state=$state, class=${codec.packetClass.simpleName}" }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Packet> getCodec(state: ProtocolState, packetClass: Class<T>): PacketCodec<T>? {
        return codecByClass[CodecKey(state, packetClass)] as? PacketCodec<T>
    }

    override fun getCodec(state: ProtocolState, packetId: Int): PacketCodec<out Packet>? {
        return codecById[IdKey(state, packetId)]
    }

    /**
     * 注册编解码器并关联数据包ID（用于按ID查找）
     */
    fun <T : Packet> register(state: ProtocolState, packetId: Int, codec: PacketCodec<T>) {
        codecByClass[CodecKey(state, codec.packetClass)] = codec
        codecById[IdKey(state, packetId)] = codec
        logger.debug { "注册编解码器: state=$state, id=0x${packetId.toString(16)}, class=${codec.packetClass.simpleName}" }
    }

    private data class CodecKey(val state: ProtocolState, val packetClass: Class<out Packet>)
    private data class IdKey(val state: ProtocolState, val packetId: Int)
}
