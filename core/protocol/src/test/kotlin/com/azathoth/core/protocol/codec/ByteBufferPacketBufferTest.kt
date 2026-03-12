package com.azathoth.core.protocol.codec

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

class ByteBufferPacketBufferTest {

    @Test
    fun `test byte read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeByte(42)
        buf.writeByte(-1)
        assertEquals(42.toByte(), buf.readByte())
        assertEquals((-1).toByte(), buf.readByte())
    }

    @Test
    fun `test short read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeShort(12345)
        buf.writeShort(-1)
        assertEquals(12345.toShort(), buf.readShort())
        assertEquals((-1).toShort(), buf.readShort())
    }

    @Test
    fun `test int read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeInt(Int.MAX_VALUE)
        buf.writeInt(Int.MIN_VALUE)
        assertEquals(Int.MAX_VALUE, buf.readInt())
        assertEquals(Int.MIN_VALUE, buf.readInt())
    }

    @Test
    fun `test long read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeLong(Long.MAX_VALUE)
        buf.writeLong(Long.MIN_VALUE)
        assertEquals(Long.MAX_VALUE, buf.readLong())
        assertEquals(Long.MIN_VALUE, buf.readLong())
    }

    @Test
    fun `test float read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeFloat(3.14f)
        assertEquals(3.14f, buf.readFloat())
    }

    @Test
    fun `test double read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeDouble(3.141592653589793)
        assertEquals(3.141592653589793, buf.readDouble())
    }

    @Test
    fun `test boolean read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeBoolean(true)
        buf.writeBoolean(false)
        assertTrue(buf.readBoolean())
        assertFalse(buf.readBoolean())
    }

    @Test
    fun `test bytes read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        val data = byteArrayOf(1, 2, 3, 4, 5)
        buf.writeBytes(data)
        assertArrayEquals(data, buf.readBytes(5))
    }

    @Test
    fun `test VarInt small values`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeVarInt(0)
        buf.writeVarInt(1)
        buf.writeVarInt(127)
        assertEquals(0, buf.readVarInt())
        assertEquals(1, buf.readVarInt())
        assertEquals(127, buf.readVarInt())
    }

    @Test
    fun `test VarInt multi-byte values`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeVarInt(128)
        buf.writeVarInt(255)
        buf.writeVarInt(25565)
        buf.writeVarInt(2097151)
        assertEquals(128, buf.readVarInt())
        assertEquals(255, buf.readVarInt())
        assertEquals(25565, buf.readVarInt())
        assertEquals(2097151, buf.readVarInt())
    }

    @Test
    fun `test VarInt negative values`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeVarInt(-1)
        buf.writeVarInt(Int.MIN_VALUE)
        assertEquals(-1, buf.readVarInt())
        assertEquals(Int.MIN_VALUE, buf.readVarInt())
    }

    @Test
    fun `test VarLong small values`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeVarLong(0L)
        buf.writeVarLong(127L)
        assertEquals(0L, buf.readVarLong())
        assertEquals(127L, buf.readVarLong())
    }

    @Test
    fun `test VarLong large values`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeVarLong(2147483647L)
        buf.writeVarLong(Long.MAX_VALUE)
        assertEquals(2147483647L, buf.readVarLong())
        assertEquals(Long.MAX_VALUE, buf.readVarLong())
    }

    @Test
    fun `test VarLong negative values`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeVarLong(-1L)
        assertEquals(-1L, buf.readVarLong())
    }

    @Test
    fun `test String read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(256)
        buf.writeString("Hello, World!")
        buf.writeString("")
        buf.writeString("你好世界")
        assertEquals("Hello, World!", buf.readString())
        assertEquals("", buf.readString())
        assertEquals("你好世界", buf.readString())
    }

    @Test
    fun `test UUID read and write`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        val uuid = UUID.randomUUID()
        buf.writeUUID(uuid)
        assertEquals(uuid, buf.readUUID())
    }

    @Test
    fun `test mark and reset reader index`() {
        val buf = ByteBufferPacketBuffer.allocate(64)
        buf.writeInt(100)
        buf.writeInt(200)

        buf.markReaderIndex()
        assertEquals(100, buf.readInt())
        buf.resetReaderIndex()
        assertEquals(100, buf.readInt())
        assertEquals(200, buf.readInt())
    }

    @Test
    fun `test readableBytes and writableBytes`() {
        val buf = ByteBufferPacketBuffer.allocate(32)
        assertEquals(0, buf.readableBytes)
        assertEquals(32, buf.writableBytes)

        buf.writeInt(1)
        assertEquals(4, buf.readableBytes)
        assertEquals(28, buf.writableBytes)

        buf.readInt()
        assertEquals(0, buf.readableBytes)
    }

    @Test
    fun `test insufficient readable bytes throws`() {
        val buf = ByteBufferPacketBuffer.allocate(4)
        assertThrows(IllegalStateException::class.java) { buf.readByte() }
    }

    @Test
    fun `test insufficient writable space throws`() {
        val buf = ByteBufferPacketBuffer.allocate(2)
        assertThrows(IllegalStateException::class.java) { buf.writeInt(1) }
    }
}
