package com.azathoth.core.protocol.handler

import com.azathoth.core.protocol.packet.Packet
import com.azathoth.core.protocol.packet.PacketContext
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 默认数据包处理管道实现
 */
class DefaultPacketPipeline : PacketPipeline {

    private val inboundHandlers = LinkedHashMap<String, InboundPacketHandler>()
    private val outboundHandlers = LinkedHashMap<String, OutboundPacketHandler>()
    private val lock = Any()

    override fun addInboundHandler(name: String, handler: InboundPacketHandler) {
        synchronized(lock) {
            inboundHandlers[name] = handler
        }
        logger.debug { "添加入站处理器: $name" }
    }

    override fun addOutboundHandler(name: String, handler: OutboundPacketHandler) {
        synchronized(lock) {
            outboundHandlers[name] = handler
        }
        logger.debug { "添加出站处理器: $name" }
    }

    override fun removeHandler(name: String) {
        synchronized(lock) {
            inboundHandlers.remove(name)
            outboundHandlers.remove(name)
        }
        logger.debug { "移除处理器: $name" }
    }

    override fun getHandler(name: String): Any? {
        synchronized(lock) {
            return inboundHandlers[name] ?: outboundHandlers[name]
        }
    }

    override suspend fun processInbound(packet: Packet, context: PacketContext) {
        val handlers = synchronized(lock) { inboundHandlers.values.toList() }
        var current: Packet? = packet
        for (handler in handlers) {
            current = handler.handleInbound(current!!, context)
            if (current == null) {
                logger.debug { "入站数据包被处理器拦截" }
                return
            }
        }
    }

    override suspend fun processOutbound(packet: Packet, context: PacketContext) {
        val handlers = synchronized(lock) { outboundHandlers.values.toList() }
        var current: Packet? = packet
        for (handler in handlers) {
            current = handler.handleOutbound(current!!, context)
            if (current == null) {
                logger.debug { "出站数据包被处理器拦截" }
                return
            }
        }
    }
}
