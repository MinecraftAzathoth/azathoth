package com.azathoth.game.dungeons.instance

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.game.dungeons.template.*
import com.azathoth.game.engine.player.DefaultGamePlayer
import com.azathoth.game.engine.world.DefaultWorldManager
import com.azathoth.game.engine.world.World
import com.azathoth.game.engine.world.WorldType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

class DefaultDungeonInstanceTest {
    private lateinit var world: World
    private lateinit var template: DungeonTemplate
    private lateinit var player1: DefaultGamePlayer
    private lateinit var player2: DefaultGamePlayer

    @BeforeEach
    fun setup() = runTest {
        val worldManager = DefaultWorldManager()
        world = worldManager.createWorld("dungeon-test", WorldType.INSTANCE)
        player1 = DefaultGamePlayer(PlayerId.of("player-1"), "Player1", world)
        player2 = DefaultGamePlayer(PlayerId.of("player-2"), "Player2", world)

        template = SimpleDungeonTemplate(
            templateId = "test-dungeon",
            name = "测试副本",
            description = "测试用副本",
            minPlayers = 1,
            maxPlayers = 5,
            phases = listOf(
                SimplePhaseConfig(
                    phaseId = "phase-1",
                    name = "第一阶段",
                    order = 1,
                    objectives = listOf(
                        SimpleObjectiveConfig("obj-1", ObjectiveType.KILL_MOBS, "击杀10只怪物", 10)
                    )
                ),
                SimplePhaseConfig(
                    phaseId = "phase-2",
                    name = "Boss阶段",
                    order = 2,
                    objectives = listOf(
                        SimpleObjectiveConfig("obj-2", ObjectiveType.KILL_BOSS, "击杀Boss", 1)
                    )
                )
            ),
            timeLimits = mapOf(DungeonDifficulty.NORMAL to 30.minutes)
        )
    }

    private fun createInstance(): DefaultDungeonInstance {
        return DefaultDungeonInstance("test-instance", template, world, DungeonDifficulty.NORMAL)
    }

    @Test
    fun `new instance should be in WAITING state`() {
        val instance = createInstance()
        assertEquals(DungeonState.WAITING, instance.state)
    }

    @Test
    fun `addPlayer should work in WAITING state`() = runTest {
        val instance = createInstance()
        assertTrue(instance.addPlayer(player1))
        assertEquals(1, instance.playerCount)
    }

    @Test
    fun `start should transition to IN_PROGRESS`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.start()

        assertEquals(DungeonState.IN_PROGRESS, instance.state)
        assertNotNull(instance.startedAt)
        assertNotNull(instance.currentPhase)
        assertEquals("phase-1", instance.currentPhase!!.phaseId)
    }

    @Test
    fun `start should fail if not in WAITING state`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.start()

        assertThrows(IllegalStateException::class.java) {
            runTest { instance.start() }
        }
    }

    @Test
    fun `advancePhase should move to next phase`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.start()

        assertEquals("phase-1", instance.currentPhase!!.phaseId)
        instance.advancePhase()
        assertEquals("phase-2", instance.currentPhase!!.phaseId)
        assertEquals(1, instance.progress.phasesCompleted)
    }

    @Test
    fun `advancePhase past last phase should complete dungeon`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.start()

        instance.advancePhase() // phase-1 -> phase-2
        instance.advancePhase() // phase-2 -> complete

        assertEquals(DungeonState.COMPLETED, instance.state)
    }

    @Test
    fun `fail should transition to FAILED`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.start()
        instance.fail("超时")

        assertEquals(DungeonState.FAILED, instance.state)
    }

    @Test
    fun `close should transition to CLOSED and remove players`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.addPlayer(player2)
        instance.start()
        instance.close()

        assertEquals(DungeonState.CLOSED, instance.state)
        assertEquals(0, instance.playerCount)
    }

    @Test
    fun `reset should return to WAITING state`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.start()
        instance.advancePhase()
        instance.reset()

        assertEquals(DungeonState.WAITING, instance.state)
        assertNull(instance.startedAt)
        assertEquals(0, instance.progress.phasesCompleted)
    }

    @Test
    fun `removing all players during IN_PROGRESS should fail dungeon`() = runTest {
        val instance = createInstance()
        instance.addPlayer(player1)
        instance.start()
        instance.removePlayer(player1, "断线")

        assertEquals(DungeonState.FAILED, instance.state)
    }

    @Test
    fun `progress should track phases`() = runTest {
        val instance = createInstance()
        assertEquals(2, instance.progress.totalPhases)
        assertEquals(0, instance.progress.phasesCompleted)
    }

    @Test
    fun `instance manager should create and track instances`() = runTest {
        val manager = DefaultDungeonInstanceManager { template ->
            val wm = DefaultWorldManager()
            wm.createWorld("dungeon-${template.templateId}", WorldType.INSTANCE)
        }

        val instance = manager.createInstance(template, DungeonDifficulty.NORMAL, player1)
        assertNotNull(instance)
        assertNotNull(manager.getInstance(instance.instanceId))
        assertNotNull(manager.getPlayerInstance(player1.playerId))
        assertEquals(1, manager.getActiveInstances().size)
    }

    @Test
    fun `instance manager should close and cleanup`() = runTest {
        val manager = DefaultDungeonInstanceManager { template ->
            val wm = DefaultWorldManager()
            wm.createWorld("dungeon-${template.templateId}", WorldType.INSTANCE)
        }

        val instance = manager.createInstance(template, DungeonDifficulty.NORMAL, player1)
        manager.closeInstance(instance.instanceId)

        assertNull(manager.getInstance(instance.instanceId))
        assertEquals(0, manager.getActiveInstances().size)
    }
}
