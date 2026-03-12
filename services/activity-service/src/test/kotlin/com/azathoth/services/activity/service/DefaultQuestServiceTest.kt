package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.services.activity.model.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultQuestServiceTest {

    private lateinit var service: DefaultQuestService

    private val testQuest = SimpleQuestInfo(
        questId = "test_quest",
        name = "测试任务",
        description = "测试用任务",
        type = QuestType.MAIN,
        objectives = listOf(
            SimpleQuestObjective("obj1", "击杀怪物", "kill", 5),
            SimpleQuestObjective("obj2", "收集物品", "collect", 3)
        ),
        rewards = listOf(
            SimpleQuestReward("gold", 100, experience = 50)
        )
    )

    @BeforeEach
    fun setup() {
        service = DefaultQuestService()
        service.registerQuest(testQuest)
    }

    @Test
    fun `listAvailableQuests returns quests`() = runTest {
        val result = service.listAvailableQuests(PlayerId("player1"))
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listAvailableQuests filters by type`() = runTest {
        val result = service.listAvailableQuests(PlayerId("player1"), QuestType.DAILY)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getQuest returns quest by id`() = runTest {
        val result = service.getQuest("test_quest")
        assertTrue(result.isSuccess)
        assertEquals("测试任务", result.getOrNull()!!.name)
    }

    @Test
    fun `acceptQuest creates progress in IN_PROGRESS state`() = runTest {
        val player = PlayerId("player1")
        val result = service.acceptQuest(player, "test_quest")
        assertTrue(result.isSuccess)
        assertEquals(QuestState.IN_PROGRESS, result.getOrNull()!!.state)
    }

    @Test
    fun `acceptQuest fails for unknown quest`() = runTest {
        val result = service.acceptQuest(PlayerId("player1"), "unknown")
        assertTrue(result.isFailure)
    }

    @Test
    fun `updateQuestProgress updates objective`() = runTest {
        val player = PlayerId("player1")
        service.acceptQuest(player, "test_quest")

        val result = service.updateQuestProgress(player, "test_quest", "obj1", 3)
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrNull()!!.objectiveProgress["obj1"])
        assertEquals(QuestState.IN_PROGRESS, result.getOrNull()!!.state)
    }

    @Test
    fun `updateQuestProgress auto-completes when all objectives met`() = runTest {
        val player = PlayerId("player1")
        service.acceptQuest(player, "test_quest")

        service.updateQuestProgress(player, "test_quest", "obj1", 5)
        val result = service.updateQuestProgress(player, "test_quest", "obj2", 3)

        assertTrue(result.isSuccess)
        assertEquals(QuestState.COMPLETED, result.getOrNull()!!.state)
        assertNotNull(result.getOrNull()!!.completedAt)
    }

    @Test
    fun `abandonQuest resets to AVAILABLE`() = runTest {
        val player = PlayerId("player1")
        service.acceptQuest(player, "test_quest")

        val result = service.abandonQuest(player, "test_quest")
        assertTrue(result.isSuccess)

        val progress = service.getPlayerQuestProgress(player, "test_quest")
        assertEquals(QuestState.AVAILABLE, progress.getOrNull()!!.state)
    }

    @Test
    fun `completeQuest returns rewards`() = runTest {
        val player = PlayerId("player1")
        service.acceptQuest(player, "test_quest")
        service.updateQuestProgress(player, "test_quest", "obj1", 5)
        service.updateQuestProgress(player, "test_quest", "obj2", 3)

        val result = service.completeQuest(player, "test_quest")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.completed)
        assertEquals(1, result.getOrNull()!!.rewards.size)
    }

    @Test
    fun `completeQuest fails if not completed`() = runTest {
        val player = PlayerId("player1")
        service.acceptQuest(player, "test_quest")

        val result = service.completeQuest(player, "test_quest")
        assertTrue(result.isFailure)
    }

    @Test
    fun `claimQuestRewards transitions to CLAIMED`() = runTest {
        val player = PlayerId("player1")
        service.acceptQuest(player, "test_quest")
        service.updateQuestProgress(player, "test_quest", "obj1", 5)
        service.updateQuestProgress(player, "test_quest", "obj2", 3)

        val result = service.claimQuestRewards(player, "test_quest")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.success)

        val progress = service.getPlayerQuestProgress(player, "test_quest")
        assertEquals(QuestState.CLAIMED, progress.getOrNull()!!.state)
    }

    @Test
    fun `getActiveQuests returns only IN_PROGRESS quests`() = runTest {
        val player = PlayerId("player1")
        service.acceptQuest(player, "test_quest")

        val result = service.getActiveQuests(player)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `refreshDailyQuests resets daily quest progress`() = runTest {
        val dailyQuest = SimpleQuestInfo(
            questId = "daily1",
            name = "每日任务",
            description = "每日",
            type = QuestType.DAILY,
            objectives = listOf(SimpleQuestObjective("d1", "击杀", "kill", 1)),
            rewards = listOf(SimpleQuestReward("gold", 10))
        )
        service.registerQuest(dailyQuest)

        val player = PlayerId("player1")
        service.acceptQuest(player, "daily1")

        val result = service.refreshDailyQuests(player)
        assertTrue(result.isSuccess)

        val progress = service.getPlayerQuestProgress(player, "daily1")
        assertTrue(progress.isFailure) // 进度已被清除
    }
}
