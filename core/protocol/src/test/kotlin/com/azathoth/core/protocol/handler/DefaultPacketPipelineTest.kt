package com.azathoth.core.protocol.handler

import com.azathoth.core.protocol.packet.Packet
import com.azathoth.core.protocol.packet.PacketContext
import com.azathoth.core.protocol.packet.PacketDirection
import com.azathoth.core.protocol.packet.ProtocolState
import com.azathoth.core.protocol.packet.ClientBoundPacket
import com.azathoth.core.common.identity.PlayerId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DefaultPacketPipelineTest {

    // 简单的测试用数据包
    private data class TestPacket(
        override val packetId: Int = 0x01,
        override val direction: PacketDirection = PacketDirection.SERVERBOUND,
        override val state: ProtocolState = ProtocolState.PLAY,
        val data: String = "test"
    ) : Packet

    // 简单的测试用上下文
    private val testContext = object : PacketContext {
        override val playerId: PlayerId? = null
        override val connectionId: String = "test-conn"
        override val protocolVersion: Int = 767
        override val state: ProtocolState = ProtocolState.PLAY
        override val remoteAddress: String = "127.0.0.1"
        override suspend fun sendPacket(packet: ClientBoundPacket) {}
        override suspend fun close(reason: String) {}
    }

    @Test
    fun `test inbound handlers are called in order`() = runTest {
        val pipeline = DefaultPacketPipeline()
        val callOrder = mutableListOf<String>()

        pipeline.addInboundHandler("first", object : InboundPacketHandler {
            override suspend fun handleInbound(packet: Packet, context: PacketContext): Packet {
                callOrder.add("first")
                return packet
            }
        })
        pipeline.addInboundHandler("second", object : InboundPacketHandler {
            override suspend fun handleInbound(packet: Packet, context: PacketContext): Packet {
                callOrder.add("second")
                return packet
            }
        })

        pipeline.processInbound(TestPacket(), testContext)
        assertEquals(listOf("first", "second"), callOrder)
    }

    @Test
    fun `test inbound handler returning null stops chain`() = runTest {
        val pipeline = DefaultPacketPipeline()
        val callOrder = mutableListOf<String>()

        pipeline.addInboundHandler("blocker", object : InboundPacketHandler {
            override suspend fun handleInbound(packet: Packet, context: PacketContext): Packet? {
                callOrder.add("blocker")
                return null
            }
        })
        pipeline.addInboundHandler("after", object : InboundPacketHandler {
            override suspend fun handleInbound(packet: Packet, context: PacketContext): Packet {
                callOrder.add("after")
                return packet
            }
        })

        pipeline.processInbound(TestPacket(), testContext)
        assertEquals(listOf("blocker"), callOrder)
    }

    @Test
    fun `test outbound handlers are called in order`() = runTest {
        val pipeline = DefaultPacketPipeline()
        val callOrder = mutableListOf<String>()

        pipeline.addOutboundHandler("first", object : OutboundPacketHandler {
            override suspend fun handleOutbound(packet: Packet, context: PacketContext): Packet {
                callOrder.add("first")
                return packet
            }
        })
        pipeline.addOutboundHandler("second", object : OutboundPacketHandler {
            override suspend fun handleOutbound(packet: Packet, context: PacketContext): Packet {
                callOrder.add("second")
                return packet
            }
        })

        pipeline.processOutbound(TestPacket(), testContext)
        assertEquals(listOf("first", "second"), callOrder)
    }

    @Test
    fun `test outbound handler returning null stops chain`() = runTest {
        val pipeline = DefaultPacketPipeline()
        val callOrder = mutableListOf<String>()

        pipeline.addOutboundHandler("blocker", object : OutboundPacketHandler {
            override suspend fun handleOutbound(packet: Packet, context: PacketContext): Packet? {
                callOrder.add("blocker")
                return null
            }
        })
        pipeline.addOutboundHandler("after", object : OutboundPacketHandler {
            override suspend fun handleOutbound(packet: Packet, context: PacketContext): Packet {
                callOrder.add("after")
                return packet
            }
        })

        pipeline.processOutbound(TestPacket(), testContext)
        assertEquals(listOf("blocker"), callOrder)
    }

    @Test
    fun `test removeHandler removes from both inbound and outbound`() {
        val pipeline = DefaultPacketPipeline()
        val handler = object : InboundPacketHandler {
            override suspend fun handleInbound(packet: Packet, context: PacketContext): Packet = packet
        }
        pipeline.addInboundHandler("test", handler)
        assertNotNull(pipeline.getHandler("test"))

        pipeline.removeHandler("test")
        assertNull(pipeline.getHandler("test"))
    }

    @Test
    fun `test getHandler returns correct handler`() {
        val pipeline = DefaultPacketPipeline()
        val inbound = object : InboundPacketHandler {
            override suspend fun handleInbound(packet: Packet, context: PacketContext): Packet = packet
        }
        val outbound = object : OutboundPacketHandler {
            override suspend fun handleOutbound(packet: Packet, context: PacketContext): Packet = packet
        }

        pipeline.addInboundHandler("in", inbound)
        pipeline.addOutboundHandler("out", outbound)

        assertSame(inbound, pipeline.getHandler("in"))
        assertSame(outbound, pipeline.getHandler("out"))
        assertNull(pipeline.getHandler("nonexistent"))
    }
}
