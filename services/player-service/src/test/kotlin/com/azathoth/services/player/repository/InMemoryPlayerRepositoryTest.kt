package com.azathoth.services.player.repository

import com.azathoth.core.common.identity.PlayerId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryPlayerRepositoryTest {

    private lateinit var repo: InMemoryPlayerRepository
    private val playerId = PlayerId("test-player-1")

    @BeforeEach
    fun setup() {
        repo = InMemoryPlayerRepository()
    }

    @Test
    fun `create player successfully`() = runTest {
        val result = repo.create(playerId, "TestUser")
        assertTrue(result.isSuccess)
        val player = result.getOrNull()!!
        assertEquals("TestUser", player.username)
        assertEquals("TestUser", player.displayName)
        assertEquals(1, player.level)
    }

    @Test
    fun `create duplicate player fails`() = runTest {
        repo.create(playerId, "TestUser")
        val result = repo.create(playerId, "TestUser2")
        assertTrue(result.isFailure)
    }

    @Test
    fun `create duplicate username fails`() = runTest {
        repo.create(playerId, "TestUser")
        val result = repo.create(PlayerId("other"), "TestUser")
        assertTrue(result.isFailure)
    }

    @Test
    fun `findById returns player`() = runTest {
        repo.create(playerId, "TestUser")
        val found = repo.findById(playerId)
        assertNotNull(found)
        assertEquals("TestUser", found!!.username)
    }

    @Test
    fun `findById returns null for missing`() = runTest {
        assertNull(repo.findById(playerId))
    }

    @Test
    fun `findByUsername works`() = runTest {
        repo.create(playerId, "TestUser")
        assertNotNull(repo.findByUsername("TestUser"))
        assertNull(repo.findByUsername("Unknown"))
    }

    @Test
    fun `delete removes player and stats`() = runTest {
        repo.create(playerId, "TestUser")
        assertTrue(repo.exists(playerId))
        val result = repo.delete(playerId)
        assertTrue(result.isSuccess)
        assertFalse(repo.exists(playerId))
        assertNull(repo.getStats(playerId))
    }

    @Test
    fun `delete missing player fails`() = runTest {
        val result = repo.delete(playerId)
        assertTrue(result.isFailure)
    }

    @Test
    fun `exists and existsByUsername`() = runTest {
        assertFalse(repo.exists(playerId))
        assertFalse(repo.existsByUsername("TestUser"))
        repo.create(playerId, "TestUser")
        assertTrue(repo.exists(playerId))
        assertTrue(repo.existsByUsername("TestUser"))
    }

    @Test
    fun `stats created with player`() = runTest {
        repo.create(playerId, "TestUser")
        val stats = repo.getStats(playerId)
        assertNotNull(stats)
        assertEquals(0, stats!!.mobsKilled)
    }

    @Test
    fun `updateLastLogin updates timestamp`() = runTest {
        repo.create(playerId, "TestUser")
        val before = repo.findById(playerId)!!.lastLoginAt
        Thread.sleep(10)
        repo.updateLastLogin(playerId)
        val after = repo.findById(playerId)!!.lastLoginAt
        assertTrue(after >= before)
    }

    @Test
    fun `addOnlineTime accumulates`() = runTest {
        repo.create(playerId, "TestUser")
        repo.addOnlineTime(playerId, 100)
        repo.addOnlineTime(playerId, 50)
        assertEquals(150, repo.findById(playerId)!!.totalOnlineTime)
    }

    @Test
    fun `search by keyword`() = runTest {
        repo.create(PlayerId("p1"), "Alice")
        repo.create(PlayerId("p2"), "Bob")
        repo.create(PlayerId("p3"), "AliceWonder")
        val results = repo.search("alice")
        assertEquals(2, results.size)
    }

    @Test
    fun `getLeaderboard by level`() = runTest {
        repo.create(PlayerId("p1"), "Low")
        repo.create(PlayerId("p2"), "High")
        val high = repo.findById(PlayerId("p2")) as SimplePlayerEntity
        (repo as InMemoryPlayerRepository).save(high.copy(level = 50))
        val board = repo.getLeaderboard(LeaderboardType.LEVEL, 10)
        assertEquals("High", board.first().username)
    }
}
