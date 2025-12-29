package com.azathoth.client.network

/**
 * 网络通信接口
 */
interface ClientNetworkHandler {
    /**
     * 连接状态
     */
    val isConnected: Boolean

    /**
     * 发送自定义数据包
     */
    fun sendPacket(channel: String, data: ByteArray)

    /**
     * 注册数据包处理器
     */
    fun registerHandler(channel: String, handler: PacketHandler)

    /**
     * 注销数据包处理器
     */
    fun unregisterHandler(channel: String)
}

/**
 * 数据包处理器
 */
fun interface PacketHandler {
    fun handle(data: ByteArray)
}
