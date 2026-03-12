package com.azathoth.core.protocol.handler

import com.azathoth.core.protocol.packet.Packet
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

/**
 * 默认数据包处理器注册表实现
 */
class DefaultPacketHandlerRegistry : PacketHandlerRegistry {

    private val handlers = ConcurrentHashMap<Class<out Packet>, PacketHandler<out Packet>>()

    override fun <T : Packet> register(handler: PacketHandler<T>) {
        handlers[handler.packetClass] = handler
        logger.debug { "注册数据包处理器: ${handler.packetClass.simpleName}" }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Packet> getHandler(packetClass: Class<T>): PacketHandler<T>? {
        return handlers[packetClass] as? PacketHandler<T>
    }

    override fun <T : Packet> unregister(packetClass: Class<T>) {
        handlers.remove(packetClass)
        logger.debug { "移除数据包处理器: ${packetClass.simpleName}" }
    }
}
