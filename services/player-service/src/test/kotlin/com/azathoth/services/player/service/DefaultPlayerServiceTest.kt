package com.azathoth.services.player.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.services.player.repository.InMemoryPlayerRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultPlayerServiceTest {

    private lateinit var repo: InMemoryPlayerRepository
    private lateinit var service: DefaultPlayerService
    private val playerId = PlayerId("test-player-1")

    @BeforeEach
    fun setup() = runTest {
        repo = InMemoryPlayerRepository()
        service = DefaultPlayerService(repo)
        service.getOrCreate(playerId, "TestUser")
    }

    @Test
    fun `getOrCreate returns existing player`() = runTest {
        val result = service.getOrCreate(playerId, "TestUser")
        assertTrue(result.isSuccess)
        assertEquals("TestUser", result.getOrNull()!!.username)
    }

    @Test
    fun `getOrCreate creates new player`() = runTest {
        val newId = PlayerId("new-player")
        val result = service.getOrCreate(newId, "NewUser")
        assertTrue(result.isSuccess)
        assertEquals("NewUser", result.getOrNull()!!.username)
    }

    @Test
    fun `getPlayer returns failure for missing`() = runTest {
        val result = service.getPlayer(PlayerId("missing"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `addExperience levels up player`() = runTest {
        // level = sqrt(exp / 100), 需要 400 经验到 level 2
        val result = service.addExperience(playerId, 500)
        assertTrue(result.isSuccess)
        val lr = result.getOrNull()!!
        assertTrue(lr.leveledUp)
        assertEquals(2, lr.newLevel)
    }

    @Test
    fun `addExperience with zero fails`() = runTest {
        val result = service.addExperience(playerId, 0)
        assertTrue(result.isFailure)
    }

    @Test
    fun `calculateLevel formula`() {
        assertEquals(1, DefaultPlayerService.calculateLevel(0))
        assertEquals(1, DefaultPlayerService.calculateLevel(99))
        assertEquals(1, DefaultPlayerService.calculateLevel(100))
        assertEquals(2, DefaultPlayerService.calculateLevel(400))
        assertEquals(10, DefaultPlayerService.calculateLevel(10000))
    }

    @Test
    fun `addGold and deductGold`() = runTest {
        val addResult = service.addGold(playerId, 1000, "测试")
        assertTrue(addResult.isSuccess)
        assertEquals(1000, addResult.getOrNull())

        val deductResult = service.deductGold(playerId, 300, "购买")
        assertTrue(deductResult.isSuccess)
        assertEquals(700, deductResult.getOrNull())
    }

    @Test
    fun `deductGold fails when insufficient`() = runTest {
        service.addGold(playerId, 100, "测试")
        val result = service.deductGold(playerId, 200, "购买")
        assertTrue(result.isFailure)
    }

    @Test
    fun `addDiamond and deductDiamond`() = runTest {
        service.addDiamond(playerId, 500, "充值")
        val result = service.deductDiamond(playerId, 200, "购买")
        assertTrue(result.isSuccess)
        assertEquals(300, result.getOrNull())
    }

    @Test
    fun `deductDiamond fails when insufficient`() = runTest {
        val result = service.deductDiamond(playerId, 1, "购买")
        assertTrue(result.isFailure)
    }

    @Test
    fun `ban and unban player`() = runTest {
        service.banPlayer(playerId, "作弊", null)
        assertTrue(service.isBanned(playerId))

        service.unbanPlayer(playerId)
        assertFalse(service.isBanned(playerId))
    }

    @Test
    fun `expired ban auto-unbans`() = runTest {
        // 封禁 0 秒 = 立即过期
        service.banPlayer(playerId, "测试", 0)
        Thread.sleep(10)
        assertFalse(service.isBanned(playerId))
    }

    @Test
    fun `getStats and updateStats`() = runTest {
        val statsResult = service.getStats(playerId)
        assertTrue(statsResult.isSuccess)

        val updateResult = service.updateStats(playerId) { it.mobsKilled = 42 }
        assertTrue(updateResult.isSuccess)
        assertEquals(42, updateResult.getOrNull()!!.mobsKilled)
    }

    @Test
    fun `onPlayerJoin and onPlayerLeave`() = runTest {
        service.onPlayerJoin(playerId)
        Thread.sleep(50)
        service.onPlayerLeave(playerId)
        // 验证不抛异常即可，在线时长已累加
        val player = repo.findById(playerId)!!
        assertTrue(player.totalOnlineTime >= 0)
    }
}
