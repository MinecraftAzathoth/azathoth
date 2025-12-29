package com.azathoth.core.protocol.codec

import com.azathoth.core.protocol.packet.Packet
import com.azathoth.core.protocol.packet.ProtocolState
import java.nio.ByteBuffer

/**
 * 数据包编解码器
 */
interface PacketCodec<T : Packet> {
    /** 对应的数据包类 */
    val packetClass: Class<T>
    
    /** 编码数据包 */
    fun encode(packet: T, buffer: PacketBuffer)
    
    /** 解码数据包 */
    fun decode(buffer: PacketBuffer): T
}

/**
 * 数据包缓冲区
 */
interface PacketBuffer {
    /** 底层字节缓冲区 */
    val buffer: ByteBuffer
    
    /** 可读字节数 */
    val readableBytes: Int
    
    /** 可写字节数 */
    val writableBytes: Int
    
    /** 读取位置 */
    var readerIndex: Int
    
    /** 写入位置 */
    var writerIndex: Int
    
    // 基础类型读取
    fun readByte(): Byte
    fun readShort(): Short
    fun readInt(): Int
    fun readLong(): Long
    fun readFloat(): Float
    fun readDouble(): Double
    fun readBoolean(): Boolean
    fun readBytes(length: Int): ByteArray
    
    // VarInt/VarLong (Minecraft 协议)
    fun readVarInt(): Int
    fun readVarLong(): Long
    
    // 字符串
    fun readString(maxLength: Int = 32767): String
    
    // UUID
    fun readUUID(): java.util.UUID
    
    // 基础类型写入
    fun writeByte(value: Byte): PacketBuffer
    fun writeShort(value: Short): PacketBuffer
    fun writeInt(value: Int): PacketBuffer
    fun writeLong(value: Long): PacketBuffer
    fun writeFloat(value: Float): PacketBuffer
    fun writeDouble(value: Double): PacketBuffer
    fun writeBoolean(value: Boolean): PacketBuffer
    fun writeBytes(bytes: ByteArray): PacketBuffer
    
    // VarInt/VarLong
    fun writeVarInt(value: Int): PacketBuffer
    fun writeVarLong(value: Long): PacketBuffer
    
    // 字符串
    fun writeString(value: String): PacketBuffer
    
    // UUID
    fun writeUUID(uuid: java.util.UUID): PacketBuffer
    
    /** 重置读取位置 */
    fun resetReaderIndex()
    
    /** 标记读取位置 */
    fun markReaderIndex()
    
    /** 释放缓冲区 */
    fun release()
}

/**
 * 编解码器注册表
 */
interface CodecRegistry {
    /** 注册编解码器 */
    fun <T : Packet> register(state: ProtocolState, codec: PacketCodec<T>)
    
    /** 获取编解码器 */
    fun <T : Packet> getCodec(state: ProtocolState, packetClass: Class<T>): PacketCodec<T>?
    
    /** 获取编解码器（通过包ID） */
    fun getCodec(state: ProtocolState, packetId: Int): PacketCodec<out Packet>?
}
