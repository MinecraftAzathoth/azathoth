package com.azathoth.services.chat.channel

import com.azathoth.core.common.identity.PlayerId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultChannelManagerTest {

    private lateinit var manager: DefaultChannelManager
    private val player1 = PlayerId("player-1")

    @BeforeEach
    fun setup() {
        manager = DefaultChannelManager()
    }

    @Test
    fun `default world channel exists`() {
        val defaultChannel = manager.getDefaultChannel()
        assertNotNull(defaultChannel)
        assertEquals(ChannelType.WORLD, defaultChannel.type)
    }

    @Test
    fun `getChannel returns world channel`() {
        val channel = manager.getChannel("world")
        assertNotNull(channel)
        assertEquals("世界频道", channel!!.name)
    }

    @Test
    fun `getChannel returns null for unknown`() {
        assertNull(manager.getChannel("nonexistent"))
    }

    @Test
    fun `createChannel adds new channel`() = runTest {
        val config = SimpleChannelConfig(name = "公会频道", type = ChannelType.GUILD)
        val channel = manager.createChannel(config)
        assertNotNull(channel)
        assertEquals("公会频道", channel.name)
        assertEquals(ChannelType.GUILD, channel.type)
        // 可以通过 ID 获取
        assertNotNull(manager.getChannel(channel.channelId))
    }

    @Test
    fun `deleteChannel removes channel`() = runTest {
        val config = SimpleChannelConfig(name = "临时频道", type = ChannelType.CUSTOM)
        val channel = manager.createChannel(config)
        manager.deleteChannel(channel.channelId)
        assertNull(manager.getChannel(channel.channelId))
    }

    @Test
    fun `deleteChannel does not remove default`() = runTest {
        manager.deleteChannel("world")
        assertNotNull(manager.getChannel("world"))
    }

    @Test
    fun `getChannelsByType filters correctly`() = runTest {
        manager.createChannel(SimpleChannelConfig(name = "G1", type = ChannelType.GUILD))
        manager.createChannel(SimpleChannelConfig(name = "G2", type = ChannelType.GUILD))
        manager.createChannel(SimpleChannelConfig(name = "P1", type = ChannelType.PARTY))

        val guilds = manager.getChannelsByType(ChannelType.GUILD)
        assertEquals(2, guilds.size)

        val worlds = manager.getChannelsByType(ChannelType.WORLD)
        assertEquals(1, worlds.size)
    }

    @Test
    fun `getPlayerChannels returns channels player is in`() = runTest {
        val ch1 = manager.createChannel(SimpleChannelConfig(name = "Ch1", type = ChannelType.GUILD))
        val ch2 = manager.createChannel(SimpleChannelConfig(name = "Ch2", type = ChannelType.PARTY))
        manager.createChannel(SimpleChannelConfig(name = "Ch3", type = ChannelType.CUSTOM))

        ch1.addMember(player1)
        ch2.addMember(player1)

        val playerChannels = manager.getPlayerChannels(player1)
        assertEquals(2, playerChannels.size)
    }
}
