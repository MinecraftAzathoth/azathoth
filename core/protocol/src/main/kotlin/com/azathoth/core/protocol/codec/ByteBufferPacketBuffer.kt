package com.azathoth.core.protocol.codec

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * 基于 java.nio.ByteBuffer 的 PacketBuffer 实现
 */
class ByteBufferPacketBuffer(override val buffer: ByteBuffer) : PacketBuffer {

    override var readerIndex: Int = 0
    override var writerIndex: Int = 0

    private var markedReaderIndex: Int = 0

    override val readableBytes: Int get() = writerIndex - readerIndex
    override val writableBytes: Int get() = buffer.capacity() - writerIndex

    // ---- 基础类型读取 ----

    override fun readByte(): Byte {
        check(readableBytes >= 1) { "没有足够的可读字节" }
        return buffer.get(readerIndex++).also {}
    }

    override fun readShort(): Short {
        check(readableBytes >= 2) { "没有足够的可读字节" }
        val value = buffer.getShort(readerIndex)
        readerIndex += 2
        return value
    }

    override fun readInt(): Int {
        check(readableBytes >= 4) { "没有足够的可读字节" }
        val value = buffer.getInt(readerIndex)
        readerIndex += 4
        return value
    }

    override fun readLong(): Long {
        check(readableBytes >= 8) { "没有足够的可读字节" }
        val value = buffer.getLong(readerIndex)
        readerIndex += 8
        return value
    }

    override fun readFloat(): Float {
        check(readableBytes >= 4) { "没有足够的可读字节" }
        val value = buffer.getFloat(readerIndex)
        readerIndex += 4
        return value
    }

    override fun readDouble(): Double {
        check(readableBytes >= 8) { "没有足够的可读字节" }
        val value = buffer.getDouble(readerIndex)
        readerIndex += 8
        return value
    }

    override fun readBoolean(): Boolean = readByte() != 0.toByte()

    override fun readBytes(length: Int): ByteArray {
        check(readableBytes >= length) { "没有足够的可读字节" }
        val bytes = ByteArray(length)
        for (i in 0 until length) {
            bytes[i] = buffer.get(readerIndex + i)
        }
        readerIndex += length
        return bytes
    }

    // ---- VarInt / VarLong ----

    override fun readVarInt(): Int {
        var result = 0
        var shift = 0
        while (true) {
            val b = readByte().toInt()
            result = result or ((b and 0x7F) shl shift)
            if (b and 0x80 == 0) break
            shift += 7
            check(shift < 32) { "VarInt 过大" }
        }
        return result
    }

    override fun readVarLong(): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = readByte().toInt()
            result = result or ((b.toLong() and 0x7F) shl shift)
            if (b and 0x80 == 0) break
            shift += 7
            check(shift < 64) { "VarLong 过大" }
        }
        return result
    }

    // ---- 字符串 ----

    override fun readString(maxLength: Int): String {
        val length = readVarInt()
        check(length in 0..maxLength * 4) { "字符串长度超出范围: $length" }
        val bytes = readBytes(length)
        val str = String(bytes, StandardCharsets.UTF_8)
        check(str.length <= maxLength) { "字符串字符数超出最大长度: ${str.length} > $maxLength" }
        return str
    }

    // ---- UUID ----

    override fun readUUID(): UUID {
        val most = readLong()
        val least = readLong()
        return UUID(most, least)
    }

    // ---- 基础类型写入 ----

    override fun writeByte(value: Byte): PacketBuffer {
        ensureWritable(1)
        buffer.put(writerIndex++, value)
        return this
    }

    override fun writeShort(value: Short): PacketBuffer {
        ensureWritable(2)
        buffer.putShort(writerIndex, value)
        writerIndex += 2
        return this
    }

    override fun writeInt(value: Int): PacketBuffer {
        ensureWritable(4)
        buffer.putInt(writerIndex, value)
        writerIndex += 4
        return this
    }

    override fun writeLong(value: Long): PacketBuffer {
        ensureWritable(8)
        buffer.putLong(writerIndex, value)
        writerIndex += 8
        return this
    }

    override fun writeFloat(value: Float): PacketBuffer {
        ensureWritable(4)
        buffer.putFloat(writerIndex, value)
        writerIndex += 4
        return this
    }

    override fun writeDouble(value: Double): PacketBuffer {
        ensureWritable(8)
        buffer.putDouble(writerIndex, value)
        writerIndex += 8
        return this
    }

    override fun writeBoolean(value: Boolean): PacketBuffer {
        writeByte(if (value) 1.toByte() else 0.toByte())
        return this
    }

    override fun writeBytes(bytes: ByteArray): PacketBuffer {
        ensureWritable(bytes.size)
        for (i in bytes.indices) {
            buffer.put(writerIndex + i, bytes[i])
        }
        writerIndex += bytes.size
        return this
    }

    // ---- VarInt / VarLong 写入 ----

    override fun writeVarInt(value: Int): PacketBuffer {
        var v = value
        while (true) {
            if (v and 0x7F.inv() == 0) {
                writeByte(v.toByte())
                return this
            }
            writeByte(((v and 0x7F) or 0x80).toByte())
            v = v ushr 7
        }
    }

    override fun writeVarLong(value: Long): PacketBuffer {
        var v = value
        while (true) {
            if (v and 0x7FL.inv() == 0L) {
                writeByte(v.toByte())
                return this
            }
            writeByte(((v and 0x7F) or 0x80).toByte())
            v = v ushr 7
        }
    }

    // ---- 字符串写入 ----

    override fun writeString(value: String): PacketBuffer {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        writeVarInt(bytes.size)
        writeBytes(bytes)
        return this
    }

    // ---- UUID 写入 ----

    override fun writeUUID(uuid: UUID): PacketBuffer {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
        return this
    }

    // ---- 标记 / 重置 ----

    override fun markReaderIndex() {
        markedReaderIndex = readerIndex
    }

    override fun resetReaderIndex() {
        readerIndex = markedReaderIndex
    }

    override fun release() {
        // ByteBuffer 由 GC 管理，无需显式释放
    }

    private fun ensureWritable(bytes: Int) {
        check(writableBytes >= bytes) { "没有足够的可写空间: 需要 $bytes, 剩余 $writableBytes" }
    }

    companion object {
        fun allocate(capacity: Int): ByteBufferPacketBuffer {
            return ByteBufferPacketBuffer(ByteBuffer.allocate(capacity))
        }
    }
}
