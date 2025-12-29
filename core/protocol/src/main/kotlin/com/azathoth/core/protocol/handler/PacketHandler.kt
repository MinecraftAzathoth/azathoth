package com.azathoth.core.protocol.handler

import com.azathoth.core.protocol.packet.Packet
import com.azathoth.core.protocol.packet.PacketContext

/**
 * 数据包处理器
 */
interface PacketHandler<T : Packet> {
    /** 处理的数据包类型 */
    val packetClass: Class<T>
    
    /** 处理数据包 */
    suspend fun handle(packet: T, context: PacketContext)
}

/**
 * 数据包处理器注册表
 */
interface PacketHandlerRegistry {
    /** 注册处理器 */
    fun <T : Packet> register(handler: PacketHandler<T>)
    
    /** 获取处理器 */
    fun <T : Packet> getHandler(packetClass: Class<T>): PacketHandler<T>?
    
    /** 移除处理器 */
    fun <T : Packet> unregister(packetClass: Class<T>)
}

/**
 * 数据包处理管道
 */
interface PacketPipeline {
    /** 添加入站处理器 */
    fun addInboundHandler(name: String, handler: InboundPacketHandler)
    
    /** 添加出站处理器 */
    fun addOutboundHandler(name: String, handler: OutboundPacketHandler)
    
    /** 移除处理器 */
    fun removeHandler(name: String)
    
    /** 获取处理器 */
    fun getHandler(name: String): Any?
    
    /** 处理入站数据包 */
    suspend fun processInbound(packet: Packet, context: PacketContext)
    
    /** 处理出站数据包 */
    suspend fun processOutbound(packet: Packet, context: PacketContext)
}

/**
 * 入站数据包处理器
 */
interface InboundPacketHandler {
    /** 处理入站数据包 */
    suspend fun handleInbound(packet: Packet, context: PacketContext): Packet?
}

/**
 * 出站数据包处理器
 */
interface OutboundPacketHandler {
    /** 处理出站数据包 */
    suspend fun handleOutbound(packet: Packet, context: PacketContext): Packet?
}

/**
 * 数据包拦截器
 */
interface PacketInterceptor {
    /** 优先级 */
    val priority: Int get() = 0
    
    /** 拦截入站数据包，返回 false 表示拦截 */
    suspend fun interceptInbound(packet: Packet, context: PacketContext): Boolean = true
    
    /** 拦截出站数据包，返回 false 表示拦截 */
    suspend fun interceptOutbound(packet: Packet, context: PacketContext): Boolean = true
}
