package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.services.activity.model.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultAchievementServiceTest {

    private lateinit var service: DefaultAchievementService

    private val testAchievement = SimpleAchievementInfo(
        achievementId = "test_achievement",
        name = "测试成就",
        description = "测试用成就",
        category = AchievementCategory.COMBAT,
        points = 10,
        icon = "sword",
        tiers = listOf(
            SimpleAchievementTier(1, "铜", 10, listOf(SimpleQuestReward("gold", 50))),
            SimpleAchievementTier(2, "银", 50, listOf(SimpleQuestReward("gold", 200))),
            SimpleAchievementTier(3, "金", 100, listOf(SimpleQuestReward("gold", 500)))
        )
    )

    @BeforeEach
    fun setup() {
        service = DefaultAchievementService()
        service.registerAchievement(testAchievement)
    }

    @Test
    fun `listAchievements returns all achievements`() = runTest {
        val result = service.listAchievements()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listAchievements filters by category`() = runTest {
        val result = service.listAchievements(AchievementCategory.EXPLORATION)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getAchievement returns achievement by id`() = runTest {
        val result = service.getAchievement("test_achievement")
        assertTrue(result.isSuccess)
        assertEquals("测试成就", result.getOrNull()!!.name)
    }

    @Test
    fun `getPlayerAchievementProgress returns zero progress for new player`() = runTest {
        val result = service.getPlayerAchievementProgress(PlayerId("player1"), "test_achievement")
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.currentProgress)
        assertEquals(0, result.getOrNull()!!.currentTier)
    }

    @Test
    fun `updateAchievementProgress increases progress`() = runTest {
        val player = PlayerId("player1")
        val result = service.updateAchievementProgress(player, "test_achievement", 5)
        assertTrue(result.isSuccess)
        assertEquals(5, result.getOrNull()!!.newProgress)
        assertNull(result.getOrNull()!!.tierUnlocked)
        assertFalse(result.getOrNull()!!.completed)
    }

    @Test
    fun `updateAchievementProgress unlocks tier`() = runTest {
        val player = PlayerId("player1")
        val result = service.updateAchievementProgress(player, "test_achievement", 15)
        assertTrue(result.isSuccess)
        assertEquals(15, result.getOrNull()!!.newProgress)
        assertEquals(1, result.getOrNull()!!.tierUnlocked)
        assertFalse(result.getOrNull()!!.completed)
    }

    @Test
    fun `updateAchievementProgress unlocks multiple tiers at once`() = runTest {
        val player = PlayerId("player1")
        val result = service.updateAchievementProgress(player, "test_achievement", 60)
        assertTrue(result.isSuccess)
        assertEquals(60, result.getOrNull()!!.newProgress)
        assertEquals(2, result.getOrNull()!!.tierUnlocked)
    }

    @Test
    fun `updateAchievementProgress completes achievement at max tier`() = runTest {
        val player = PlayerId("player1")
        val result = service.updateAchievementProgress(player, "test_achievement", 100)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.completed)
        assertEquals(3, result.getOrNull()!!.tierUnlocked)
    }

    @Test
    fun `getPlayerAchievements returns player progress`() = runTest {
        val player = PlayerId("player1")
        service.updateAchievementProgress(player, "test_achievement", 15)

        val result = service.getPlayerAchievements(player)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `getPlayerAchievements filters by category`() = runTest {
        val player = PlayerId("player1")
        service.updateAchievementProgress(player, "test_achievement", 15)

        val result = service.getPlayerAchievements(player, AchievementCategory.EXPLORATION)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getAchievementPoints returns total points`() = runTest {
        val player = PlayerId("player1")
        service.updateAchievementProgress(player, "test_achievement", 15) // 解锁tier 1

        val result = service.getAchievementPoints(player)
        assertTrue(result.isSuccess)
        assertEquals(10, result.getOrNull()) // 10 points per completed tier
    }

    @Test
    fun `updateAchievementProgress fails for unknown achievement`() = runTest {
        val result = service.updateAchievementProgress(PlayerId("player1"), "unknown", 10)
        assertTrue(result.isFailure)
    }
}
