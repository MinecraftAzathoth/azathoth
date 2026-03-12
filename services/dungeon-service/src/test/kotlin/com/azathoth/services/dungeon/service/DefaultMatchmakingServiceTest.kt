package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.services.dungeon.model.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class DefaultMatchmakingServiceTest {

    private lateinit var dungeonService: DefaultDungeonService
    private lateinit var matchmakingService: DefaultMatchmakingService

    private val testTemplate = SimpleDungeonTemplateInfo(
        templateId = "test_dungeon",
        name = "测试副本",
        description = "测试用副本",
        minPlayers = 2,
        maxPlayers = 4,
        recommendedLevel = 10,
        minLevel = 5,
        supportedDifficulties = listOf(DungeonDifficulty.NORMAL),
        timeLimit = Duration.ofMinutes(30),
        dailyEntryLimit = 3,
        weeklyEntryLimit = 15
    )

    @BeforeEach
    fun setup() {
        dungeonService = DefaultDungeonService()
        dungeonService.registerTemplate(testTemplate)
        matchmakingService = DefaultMatchmakingService(dungeonService)
    }

    @Test
    fun `joinQueue queues player when not enough players`() = runTest {
        val request = SimpleMatchmakingRequest(
            playerId = PlayerId("player1"),
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            preferences = SimpleMatchmakingPreferences()
        )

        val result = matchmakingService.joinQueue(request)
        assertTrue(result.isSuccess)
        assertEquals(MatchmakingStatus.QUEUED, result.getOrNull()!!.status)
    }

    @Test
    fun `joinQueue matches when enough players`() = runTest {
        val prefs = SimpleMatchmakingPreferences()

        val req1 = SimpleMatchmakingRequest(
            playerId = PlayerId("player1"),
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            preferences = prefs
        )
        matchmakingService.joinQueue(req1)

        val req2 = SimpleMatchmakingRequest(
            playerId = PlayerId("player2"),
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            preferences = prefs
        )
        val result = matchmakingService.joinQueue(req2)
        assertTrue(result.isSuccess)
        assertEquals(MatchmakingStatus.FOUND, result.getOrNull()!!.status)
        assertNotNull(result.getOrNull()!!.instanceId)
    }

    @Test
    fun `joinQueue fails for duplicate player`() = runTest {
        val request = SimpleMatchmakingRequest(
            playerId = PlayerId("player1"),
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            preferences = SimpleMatchmakingPreferences()
        )

        matchmakingService.joinQueue(request)
        val result = matchmakingService.joinQueue(request)
        assertTrue(result.isFailure)
    }

    @Test
    fun `leaveQueue removes player from queue`() = runTest {
        val request = SimpleMatchmakingRequest(
            playerId = PlayerId("player1"),
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            preferences = SimpleMatchmakingPreferences()
        )

        matchmakingService.joinQueue(request)
        val result = matchmakingService.leaveQueue(PlayerId("player1"))
        assertTrue(result.isSuccess)

        val status = matchmakingService.getQueueStatus(PlayerId("player1"))
        assertTrue(status.isFailure)
    }

    @Test
    fun `getQueueStatus returns status for queued player`() = runTest {
        val request = SimpleMatchmakingRequest(
            playerId = PlayerId("player1"),
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            preferences = SimpleMatchmakingPreferences()
        )

        matchmakingService.joinQueue(request)
        val result = matchmakingService.getQueueStatus(PlayerId("player1"))
        assertTrue(result.isSuccess)
        assertEquals(MatchmakingStatus.QUEUED, result.getOrNull()!!.status)
    }

    @Test
    fun `declineMatch removes player and cancels`() = runTest {
        val request = SimpleMatchmakingRequest(
            playerId = PlayerId("player1"),
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            preferences = SimpleMatchmakingPreferences()
        )

        val joinResult = matchmakingService.joinQueue(request)
        val matchId = joinResult.getOrNull()!!.matchId!!

        val result = matchmakingService.declineMatch(PlayerId("player1"), matchId)
        assertTrue(result.isSuccess)
    }
}
