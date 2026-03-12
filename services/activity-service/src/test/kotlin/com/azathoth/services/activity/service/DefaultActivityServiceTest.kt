package com.azathoth.services.activity.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.services.activity.model.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class DefaultActivityServiceTest {

    private lateinit var service: DefaultActivityService

    private val testActivity = SimpleActivityInfo(
        activityId = "test_activity",
        name = "测试活动",
        description = "测试用活动",
        type = ActivityType.LIMITED_TIME,
        state = ActivityState.ACTIVE,
        startTime = Instant.now(),
        endTime = Instant.now().plus(Duration.ofDays(7)),
        config = SimpleActivityConfig(minLevel = 1),
        rewards = listOf(
            SimpleActivityReward(
                rewardId = "reward1",
                name = "金币奖励",
                description = "达到100分获得",
                itemId = "gold",
                amount = 100,
                condition = SimpleRewardCondition("score", 100)
            )
        )
    )

    @BeforeEach
    fun setup() {
        service = DefaultActivityService()
        service.registerActivity(testActivity)
    }

    @Test
    fun `listActivities returns all activities`() = runTest {
        val result = service.listActivities()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listActivities filters by state`() = runTest {
        val result = service.listActivities(state = ActivityState.ENDED)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `listActivities filters by type`() = runTest {
        val result = service.listActivities(type = ActivityType.LIMITED_TIME)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `getActivity returns activity by id`() = runTest {
        val result = service.getActivity("test_activity")
        assertTrue(result.isSuccess)
        assertEquals("测试活动", result.getOrNull()!!.name)
    }

    @Test
    fun `getActivity returns failure for unknown id`() = runTest {
        val result = service.getActivity("unknown")
        assertTrue(result.isFailure)
    }

    @Test
    fun `joinActivity creates player progress`() = runTest {
        val player = PlayerId("player1")
        val result = service.joinActivity(player, "test_activity")
        assertTrue(result.isSuccess)
        assertEquals(0L, result.getOrNull()!!.score)
    }

    @Test
    fun `joinActivity fails for inactive activity`() = runTest {
        service.registerActivity(testActivity.copy(activityId = "ended", state = ActivityState.ENDED))
        val result = service.joinActivity(PlayerId("player1"), "ended")
        assertTrue(result.isFailure)
    }

    @Test
    fun `joinActivity fails for duplicate join`() = runTest {
        val player = PlayerId("player1")
        service.joinActivity(player, "test_activity")
        val result = service.joinActivity(player, "test_activity")
        assertTrue(result.isFailure)
    }

    @Test
    fun `updateProgress increases score`() = runTest {
        val player = PlayerId("player1")
        service.joinActivity(player, "test_activity")

        val result = service.updateProgress(player, "test_activity", 50)
        assertTrue(result.isSuccess)
        assertEquals(50L, result.getOrNull()!!.score)
    }

    @Test
    fun `claimReward succeeds when threshold met`() = runTest {
        val player = PlayerId("player1")
        service.joinActivity(player, "test_activity")
        service.updateProgress(player, "test_activity", 150)

        val result = service.claimReward(player, "test_activity", "reward1")
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.success)
    }

    @Test
    fun `claimReward fails when threshold not met`() = runTest {
        val player = PlayerId("player1")
        service.joinActivity(player, "test_activity")

        val result = service.claimReward(player, "test_activity", "reward1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `claimReward fails for duplicate claim`() = runTest {
        val player = PlayerId("player1")
        service.joinActivity(player, "test_activity")
        service.updateProgress(player, "test_activity", 150)
        service.claimReward(player, "test_activity", "reward1")

        val result = service.claimReward(player, "test_activity", "reward1")
        assertTrue(result.isFailure)
    }

    @Test
    fun `getLeaderboard returns sorted entries`() = runTest {
        val p1 = PlayerId("player1")
        val p2 = PlayerId("player2")
        service.joinActivity(p1, "test_activity")
        service.joinActivity(p2, "test_activity")
        service.updateProgress(p1, "test_activity", 100)
        service.updateProgress(p2, "test_activity", 200)

        val result = service.getLeaderboard("test_activity")
        assertTrue(result.isSuccess)
        val entries = result.getOrNull()!!
        assertEquals(2, entries.size)
        assertEquals(p2, entries[0].playerId)
        assertEquals(p1, entries[1].playerId)
    }
}
