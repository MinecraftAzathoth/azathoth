package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.services.dungeon.model.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class DefaultDungeonServiceTest {

    private lateinit var service: DefaultDungeonService

    private val testTemplate = SimpleDungeonTemplateInfo(
        templateId = "test_dungeon",
        name = "测试副本",
        description = "测试用副本",
        minPlayers = 1,
        maxPlayers = 5,
        recommendedLevel = 10,
        minLevel = 5,
        supportedDifficulties = listOf(DungeonDifficulty.NORMAL, DungeonDifficulty.HARD),
        timeLimit = Duration.ofMinutes(30),
        dailyEntryLimit = 3,
        weeklyEntryLimit = 15
    )

    @BeforeEach
    fun setup() {
        service = DefaultDungeonService()
        service.registerTemplate(testTemplate)
    }

    @Test
    fun `listTemplates returns all templates`() = runTest {
        val result = service.listTemplates()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()!!.size)
    }

    @Test
    fun `listTemplates filters by difficulty`() = runTest {
        val result = service.listTemplates(difficulty = DungeonDifficulty.NIGHTMARE)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `listTemplates filters by level range`() = runTest {
        val result = service.listTemplates(minLevel = 20)
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()!!.isEmpty())
    }

    @Test
    fun `getTemplate returns template by id`() = runTest {
        val result = service.getTemplate("test_dungeon")
        assertTrue(result.isSuccess)
        assertEquals("测试副本", result.getOrNull()!!.name)
    }

    @Test
    fun `getTemplate returns failure for unknown id`() = runTest {
        val result = service.getTemplate("unknown")
        assertTrue(result.isFailure)
    }

    @Test
    fun `createInstance creates a new instance`() = runTest {
        val leader = PlayerId("player1")
        val members = listOf(PlayerId("player2"))
        val result = service.createInstance("test_dungeon", DungeonDifficulty.NORMAL, leader, members)

        assertTrue(result.isSuccess)
        val instance = result.getOrNull()!!
        assertEquals("test_dungeon", instance.templateId)
        assertEquals(DungeonDifficulty.NORMAL, instance.difficulty)
        assertEquals(DungeonInstanceState.WAITING, instance.state)
        assertTrue(instance.playerIds.contains(leader))
        assertTrue(instance.playerIds.contains(PlayerId("player2")))
    }

    @Test
    fun `createInstance fails for unsupported difficulty`() = runTest {
        val result = service.createInstance(
            "test_dungeon", DungeonDifficulty.HELL, PlayerId("p1"), emptyList()
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `createInstance fails when too many players`() = runTest {
        val members = (1..10).map { PlayerId("p$it") }
        val result = service.createInstance(
            "test_dungeon", DungeonDifficulty.NORMAL, PlayerId("leader"), members
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `joinInstance adds player to instance`() = runTest {
        val createResult = service.createInstance(
            "test_dungeon", DungeonDifficulty.NORMAL, PlayerId("leader"), emptyList()
        )
        val instanceId = createResult.getOrNull()!!.instanceId

        val joinResult = service.joinInstance(instanceId, PlayerId("joiner"))
        assertTrue(joinResult.isSuccess)
        assertTrue(joinResult.getOrNull()!!.success)

        val instance = service.getInstance(instanceId).getOrNull()!!
        assertTrue(instance.playerIds.contains(PlayerId("joiner")))
    }

    @Test
    fun `leaveInstance removes player`() = runTest {
        val leader = PlayerId("leader")
        val member = PlayerId("member")
        val createResult = service.createInstance(
            "test_dungeon", DungeonDifficulty.NORMAL, leader, listOf(member)
        )
        val instanceId = createResult.getOrNull()!!.instanceId

        val leaveResult = service.leaveInstance(instanceId, member)
        assertTrue(leaveResult.isSuccess)

        val instance = service.getInstance(instanceId).getOrNull()!!
        assertFalse(instance.playerIds.contains(member))
    }

    @Test
    fun `leaveInstance closes instance when last player leaves`() = runTest {
        val leader = PlayerId("solo")
        val createResult = service.createInstance(
            "test_dungeon", DungeonDifficulty.NORMAL, leader, emptyList()
        )
        val instanceId = createResult.getOrNull()!!.instanceId

        service.leaveInstance(instanceId, leader)
        val instance = service.getInstance(instanceId).getOrNull()!!
        assertEquals(DungeonInstanceState.CLOSED, instance.state)
    }

    @Test
    fun `startInstance transitions state to IN_PROGRESS`() = runTest {
        val createResult = service.createInstance(
            "test_dungeon", DungeonDifficulty.NORMAL, PlayerId("leader"), emptyList()
        )
        val instanceId = createResult.getOrNull()!!.instanceId

        val startResult = service.startInstance(instanceId)
        assertTrue(startResult.isSuccess)
        assertEquals(DungeonInstanceState.IN_PROGRESS, startResult.getOrNull()!!.state)
    }

    @Test
    fun `completeInstance transitions state`() = runTest {
        val leader = PlayerId("leader")
        val createResult = service.createInstance(
            "test_dungeon", DungeonDifficulty.NORMAL, leader, emptyList()
        )
        val instanceId = createResult.getOrNull()!!.instanceId

        val dungeonResult = SimpleDungeonResult(
            instanceId = instanceId,
            templateId = "test_dungeon",
            difficulty = DungeonDifficulty.NORMAL,
            success = true,
            rating = DungeonRating.A,
            duration = Duration.ofMinutes(15),
            score = 1000,
            participants = listOf(
                SimpleParticipantResult(leader, 5000, 0, 0, 1.0, emptyList())
            ),
            completedAt = Instant.now()
        )

        val completeResult = service.completeInstance(instanceId, dungeonResult)
        assertTrue(completeResult.isSuccess)

        val instance = service.getInstance(instanceId).getOrNull()!!
        assertEquals(DungeonInstanceState.COMPLETED, instance.state)
    }

    @Test
    fun `getProgress returns progress for instance`() = runTest {
        val createResult = service.createInstance(
            "test_dungeon", DungeonDifficulty.NORMAL, PlayerId("leader"), emptyList()
        )
        val instanceId = createResult.getOrNull()!!.instanceId

        val progress = service.getProgress(instanceId)
        assertTrue(progress.isSuccess)
        assertEquals(instanceId, progress.getOrNull()!!.instanceId)
    }
}
