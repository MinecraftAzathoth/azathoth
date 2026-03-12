package com.azathoth.services.chat.channel

import com.azathoth.core.common.identity.PlayerId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultChatChannelTest {

    private lateinit var channel: DefaultChatChannel
    private val player1 = PlayerId("player-1")
    private val player2 = PlayerId("player-2")

    @BeforeEach
    fun setup() {
        channel = DefaultChatChannel(
            channelId = "test-channel",
            config = SimpleChannelConfig(
                name = "测试频道",
                type = ChannelType.WORLD,
                cooldownSeconds = 2
            )
        )
    }

    @Test
    fun `add and remove members`() = runTest {
        channel.addMember(player1)
        assertTrue(channel.isMember(player1))
        assertEquals(1, channel.memberCount)

        channel.removeMember(player1)
        assertFalse(channel.isMember(player1))
        assertEquals(0, channel.memberCount)
    }

    @Test
    fun `getMembers returns copy`() = runTest {
        channel.addMember(player1)
        channel.addMember(player2)
        val members = channel.getMembers()
        assertEquals(2, members.size)
        assertTrue(members.contains(player1))
    }

    @Test
    fun `canSpeak denied for non-member`() {
        val result = channel.canSpeak(player1)
        assertFalse(result.allowed)
        assertEquals(ChatDenyReason.NOT_MEMBER, result.denyReason)
    }

    @Test
    fun `canSpeak allowed for member`() = runTest {
        channel.addMember(player1)
        val result = channel.canSpeak(player1)
        assertTrue(result.allowed)
    }

    @Test
    fun `canSpeak denied for muted player`() = runTest {
        channel.addMember(player1)
        channel.mutePlayer(player1)
        val result = channel.canSpeak(player1)
        assertFalse(result.allowed)
        assertEquals(ChatDenyReason.MUTED, result.denyReason)
    }

    @Test
    fun `unmute restores speaking`() = runTest {
        channel.addMember(player1)
        channel.mutePlayer(player1)
        channel.unmutePlayer(player1)
        assertTrue(channel.canSpeak(player1).allowed)
    }

    @Test
    fun `cooldown enforced after broadcast`() = runTest {
        channel.addMember(player1)
        val msg = SimpleChatMessage(
            messageId = "msg-1",
            channelId = channel.channelId,
            senderId = player1,
            senderName = "Player1",
            content = "Hello"
        )
        channel.broadcast(msg)

        val result = channel.canSpeak(player1)
        assertFalse(result.allowed)
        assertEquals(ChatDenyReason.ON_COOLDOWN, result.denyReason)
        assertNotNull(result.cooldownRemaining)
    }

    @Test
    fun `no cooldown channel allows immediate speak`() = runTest {
        val noCooldown = DefaultChatChannel(
            channelId = "no-cd",
            config = SimpleChannelConfig(name = "无冷却", type = ChannelType.PARTY, cooldownSeconds = 0)
        )
        noCooldown.addMember(player1)
        val msg = SimpleChatMessage(
            messageId = "msg-1",
            channelId = "no-cd",
            senderId = player1,
            senderName = "Player1",
            content = "Hello"
        )
        noCooldown.broadcast(msg)
        assertTrue(noCooldown.canSpeak(player1).allowed)
    }

    @Test
    fun `channel properties from config`() {
        assertEquals("test-channel", channel.channelId)
        assertEquals("测试频道", channel.name)
        assertEquals(ChannelType.WORLD, channel.type)
        assertEquals(2, channel.cooldownSeconds)
        assertFalse(channel.persistent)
    }
}
