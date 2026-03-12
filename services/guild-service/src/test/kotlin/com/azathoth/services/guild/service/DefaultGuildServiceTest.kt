package com.azathoth.services.guild.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultGuildServiceTest {

    private lateinit var service: DefaultGuildService

    private val leader = PlayerId("leader-1")
    private val player1 = PlayerId("player-1")
    private val player2 = PlayerId("player-2")

    @BeforeEach
    fun setup() {
        service = DefaultGuildService()
    }

    @Test
    fun `createGuild should succeed`() = runTest {
        val result = service.createGuild(leader, "TestGuild", "TG")
        assertTrue(result.isSuccess)
        val guild = (result as Result.Success).value
        assertEquals("TestGuild", guild.name)
        assertEquals("TG", guild.tag)
        assertEquals(leader, guild.leaderId)
    }

    @Test
    fun `createGuild should fail if player already in guild`() = runTest {
        service.createGuild(leader, "Guild1", "G1")
        val result = service.createGuild(leader, "Guild2", "G2")
        assertTrue(result.isFailure)
    }

    @Test
    fun `createGuild should fail with duplicate name`() = runTest {
        service.createGuild(leader, "TestGuild", "TG")
        val result = service.createGuild(player1, "TestGuild", "T2")
        assertTrue(result.isFailure)
    }

    @Test
    fun `applyToGuild and join`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        // 默认不需要申请，直接加入
        val result = service.applyToGuild(player1, guild.guildId)
        assertTrue(result.isSuccess)

        val playerGuild = service.getPlayerGuild(player1)
        assertNotNull(playerGuild)
        assertEquals(guild.guildId, playerGuild!!.guildId)
    }

    @Test
    fun `kickMember should work with proper rank`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)

        val result = service.kickMember(guild.guildId, leader, player1)
        assertTrue(result.isSuccess)

        assertNull(service.getPlayerGuild(player1))
    }

    @Test
    fun `kickMember should fail without proper rank`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)
        service.applyToGuild(player2, guild.guildId)

        // player1 (NEWCOMER) 不能踢 player2 (NEWCOMER)
        val result = service.kickMember(guild.guildId, player1, player2)
        assertTrue(result.isFailure)
    }

    @Test
    fun `setMemberRank should work`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)

        val result = service.setMemberRank(guild.guildId, leader, player1, GuildRank.ELDER)
        assertTrue(result.isSuccess)

        val members = service.getMembers(guild.guildId)
        val member = members.find { it.playerId == player1 }
        assertEquals(GuildRank.ELDER, member?.rank)
    }

    @Test
    fun `setMemberRank should fail for LEADER rank`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)

        val result = service.setMemberRank(guild.guildId, leader, player1, GuildRank.LEADER)
        assertTrue(result.isFailure)
    }

    @Test
    fun `transferLeadership should work`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)

        val result = service.transferLeadership(guild.guildId, leader, player1)
        assertTrue(result.isSuccess)

        val updatedGuild = service.getGuild(guild.guildId)!!
        assertEquals(player1, updatedGuild.leaderId)

        val members = service.getMembers(guild.guildId)
        assertEquals(GuildRank.LEADER, members.find { it.playerId == player1 }?.rank)
        assertEquals(GuildRank.CO_LEADER, members.find { it.playerId == leader }?.rank)
    }

    @Test
    fun `transferLeadership should fail for non-leader`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)

        val result = service.transferLeadership(guild.guildId, player1, leader)
        assertTrue(result.isFailure)
    }

    @Test
    fun `leaveGuild should work for non-leader`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)

        val result = service.leaveGuild(player1)
        assertTrue(result.isSuccess)
        assertNull(service.getPlayerGuild(player1))
    }

    @Test
    fun `leaveGuild should fail for leader`() = runTest {
        service.createGuild(leader, "TestGuild", "TG")
        val result = service.leaveGuild(leader)
        assertTrue(result.isFailure)
    }

    @Test
    fun `disbandGuild should remove all members`() = runTest {
        val guild = (service.createGuild(leader, "TestGuild", "TG") as Result.Success).value
        service.applyToGuild(player1, guild.guildId)

        val result = service.disbandGuild(guild.guildId, leader)
        assertTrue(result.isSuccess)

        assertNull(service.getGuild(guild.guildId))
        assertNull(service.getPlayerGuild(leader))
        assertNull(service.getPlayerGuild(player1))
    }
}
