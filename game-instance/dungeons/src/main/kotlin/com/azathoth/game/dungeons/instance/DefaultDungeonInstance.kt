package com.azathoth.game.dungeons.instance

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.game.dungeons.template.DungeonTemplate
import com.azathoth.game.engine.player.GamePlayer
import com.azathoth.game.engine.world.World
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

// --- Data classes ---

class DefaultDungeonObjective(
    override val objectiveId: String,
    override val description: String,
    override val targetProgress: Int
) : DungeonObjective {
    @Volatile
    override var currentProgress: Int = 0
    override val isCompleted: Boolean get() = currentProgress >= targetProgress

    fun addProgress(amount: Int = 1) {
        currentProgress = (currentProgress + amount).coerceAtMost(targetProgress)
    }
}

class DefaultDungeonPhase(
    override val phaseId: String,
    override val name: String,
    override val order: Int,
    override val objectives: List<DefaultDungeonObjective>
) : DungeonPhase {
    override val isCompleted: Boolean
        get() = objectives.all { it.isCompleted }
}

class DefaultDungeonProgress : DungeonProgress {
    @Volatile
    override var monstersKilled: Int = 0
    @Volatile
    override var bossesKilled: Int = 0
    @Volatile
    override var phasesCompleted: Int = 0
    @Volatile
    override var totalPhases: Int = 0
    @Volatile
    override var score: Int = 0
    @Volatile
    override var deaths: Int = 0

    override val rating: DungeonRating?
        get() = when {
            score >= 10000 -> DungeonRating.S
            score >= 8000 -> DungeonRating.A
            score >= 6000 -> DungeonRating.B
            score >= 4000 -> DungeonRating.C
            score >= 2000 -> DungeonRating.D
            score > 0 -> DungeonRating.F
            else -> null
        }

    fun reset() {
        monstersKilled = 0
        bossesKilled = 0
        phasesCompleted = 0
        score = 0
        deaths = 0
    }
}

// --- DungeonInstance ---

class DefaultDungeonInstance(
    override val instanceId: String,
    override val template: DungeonTemplate,
    override val world: World,
    override val difficulty: DungeonDifficulty
) : DungeonInstance {

    override val createdAt: Long = System.currentTimeMillis()

    @Volatile
    override var startedAt: Long? = null
        private set

    @Volatile
    override var state: DungeonState = DungeonState.CREATING
        private set

    override val elapsedTime: Duration
        get() {
            val start = startedAt ?: return Duration.ZERO
            return (System.currentTimeMillis() - start).milliseconds
        }

    override val remainingTime: Duration?
        get() {
            val limit = template.getTimeLimit(difficulty) ?: return null
            val remaining = limit - elapsedTime
            return if (remaining.isPositive()) remaining else Duration.ZERO
        }

    private val players = ConcurrentHashMap<PlayerId, GamePlayer>()
    override val playerCount: Int get() = players.size
    override val maxPlayers: Int get() = template.maxPlayers

    private val phases = mutableListOf<DefaultDungeonPhase>()
    private var currentPhaseIndex = -1

    override val currentPhase: DungeonPhase?
        get() = if (currentPhaseIndex in phases.indices) phases[currentPhaseIndex] else null

    private val _progress = DefaultDungeonProgress()
    override val progress: DungeonProgress get() = _progress

    init {
        // 从模板构建阶段
        template.phases.forEachIndexed { _, phaseConfig ->
            val objectives = phaseConfig.objectives.map { obj ->
                DefaultDungeonObjective(obj.objectiveId, obj.description, obj.targetCount)
            }
            phases.add(DefaultDungeonPhase(phaseConfig.phaseId, phaseConfig.name, phaseConfig.order, objectives))
        }
        _progress.totalPhases = phases.size
        state = DungeonState.WAITING
    }

    override fun getPlayers(): Collection<GamePlayer> = players.values

    override suspend fun addPlayer(player: GamePlayer): Boolean {
        if (state != DungeonState.WAITING && state != DungeonState.IN_PROGRESS) return false
        if (playerCount >= maxPlayers) return false
        players[player.playerId] = player
        logger.info { "玩家 ${player.name} 加入副本 $instanceId" }
        return true
    }

    override suspend fun removePlayer(player: GamePlayer, reason: String) {
        players.remove(player.playerId)
        logger.info { "玩家 ${player.name} 离开副本 $instanceId: $reason" }
        if (players.isEmpty() && state == DungeonState.IN_PROGRESS) {
            fail("所有玩家已离开")
        }
    }

    override suspend fun start() {
        check(state == DungeonState.WAITING) { "无法从状态 $state 启动副本" }
        state = DungeonState.IN_PROGRESS
        startedAt = System.currentTimeMillis()
        if (phases.isNotEmpty()) currentPhaseIndex = 0
        logger.info { "副本 $instanceId 开始, 难度=$difficulty, 玩家数=$playerCount" }
    }

    override suspend fun complete() {
        check(state == DungeonState.IN_PROGRESS) { "无法从状态 $state 完成副本" }
        state = DungeonState.COMPLETED
        logger.info { "副本 $instanceId 完成! 用时=${elapsedTime}, 评级=${_progress.rating}" }
    }

    override suspend fun fail(reason: String) {
        if (state != DungeonState.IN_PROGRESS && state != DungeonState.WAITING) return
        state = DungeonState.FAILED
        logger.info { "副本 $instanceId 失败: $reason" }
    }

    override suspend fun close() {
        state = DungeonState.CLOSING
        players.values.toList().forEach { removePlayer(it, "副本关闭") }
        state = DungeonState.CLOSED
        logger.info { "副本 $instanceId 已关闭" }
    }

    override suspend fun reset() {
        state = DungeonState.WAITING
        startedAt = null
        currentPhaseIndex = -1
        _progress.reset()
        phases.forEach { phase ->
            phase.objectives.forEach { it.currentProgress = 0 }
        }
        logger.info { "副本 $instanceId 已重置" }
    }

    override suspend fun advancePhase() {
        check(state == DungeonState.IN_PROGRESS) { "副本未在进行中" }
        if (currentPhaseIndex >= 0 && currentPhaseIndex < phases.size) {
            _progress.phasesCompleted++
        }
        currentPhaseIndex++
        if (currentPhaseIndex >= phases.size) {
            complete()
        } else {
            logger.info { "副本 $instanceId 进入阶段: ${phases[currentPhaseIndex].name}" }
        }
    }
}

// --- DungeonInstanceManager ---

class DefaultDungeonInstanceManager(
    private val worldProvider: suspend (DungeonTemplate) -> World
) : DungeonInstanceManager {

    private val instances = ConcurrentHashMap<String, DefaultDungeonInstance>()
    private val playerInstances = ConcurrentHashMap<PlayerId, String>()

    override suspend fun createInstance(
        template: DungeonTemplate,
        difficulty: DungeonDifficulty,
        leader: GamePlayer
    ): DungeonInstance {
        val instanceId = UUID.randomUUID().toString()
        val world = worldProvider(template)
        val instance = DefaultDungeonInstance(instanceId, template, world, difficulty)
        instances[instanceId] = instance
        instance.addPlayer(leader)
        playerInstances[leader.playerId] = instanceId
        logger.info { "创建副本实例: $instanceId, 模板=${template.name}, 难度=$difficulty" }
        return instance
    }

    override fun getInstance(instanceId: String): DungeonInstance? = instances[instanceId]

    override fun getPlayerInstance(playerId: PlayerId): DungeonInstance? {
        val instanceId = playerInstances[playerId] ?: return null
        return instances[instanceId]
    }

    override fun getActiveInstances(): Collection<DungeonInstance> =
        instances.values.filter { it.state != DungeonState.CLOSED }

    override suspend fun closeInstance(instanceId: String) {
        val instance = instances[instanceId] ?: return
        instance.getPlayers().forEach { playerInstances.remove(it.playerId) }
        instance.close()
        instances.remove(instanceId)
    }

    override suspend fun cleanupExpiredInstances() {
        val now = System.currentTimeMillis()
        val expired = instances.values.filter { instance ->
            instance.state == DungeonState.CLOSED ||
                instance.state == DungeonState.COMPLETED ||
                instance.state == DungeonState.FAILED
        }
        for (instance in expired) {
            instance.getPlayers().forEach { playerInstances.remove(it.playerId) }
            if (instance.state != DungeonState.CLOSED) instance.close()
            instances.remove(instance.instanceId)
        }
        if (expired.isNotEmpty()) {
            logger.info { "清理了 ${expired.size} 个过期副本实例" }
        }
    }
}
