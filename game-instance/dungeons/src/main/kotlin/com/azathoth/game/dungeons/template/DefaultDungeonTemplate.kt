package com.azathoth.game.dungeons.template

import com.azathoth.game.dungeons.instance.DungeonDifficulty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * 简单副本模板实现
 */
data class SimpleDungeonTemplate(
    override val templateId: String,
    override val name: String,
    override val description: String,
    override val minPlayers: Int = 1,
    override val maxPlayers: Int = 5,
    override val recommendedLevel: Int = 1,
    override val minLevel: Int = 1,
    override val supportedDifficulties: Set<DungeonDifficulty> = setOf(DungeonDifficulty.NORMAL),
    override val worldTemplateName: String = "default",
    override val spawnPointConfig: SpawnPointConfig = SimpleSpawnPointConfig(),
    override val phases: List<PhaseConfig> = emptyList(),
    override val dailyEntryLimit: Int = 3,
    override val weeklyEntryLimit: Int = 21,
    override val cooldown: Duration = 30.minutes,
    private val timeLimits: Map<DungeonDifficulty, Duration> = emptyMap(),
    private val rewards: Map<DungeonDifficulty, RewardConfig> = emptyMap()
) : DungeonTemplate {
    override fun getTimeLimit(difficulty: DungeonDifficulty): Duration? = timeLimits[difficulty]
    override fun getRewardConfig(difficulty: DungeonDifficulty): RewardConfig =
        rewards[difficulty] ?: SimpleRewardConfig()
}

data class SimpleSpawnPointConfig(
    override val playerSpawnPoints: List<SpawnPoint> = listOf(SpawnPoint(0.0, 64.0, 0.0)),
    override val bossSpawnPoints: List<SpawnPoint> = emptyList(),
    override val mobSpawnPoints: List<SpawnPoint> = emptyList()
) : SpawnPointConfig

data class SimplePhaseConfig(
    override val phaseId: String,
    override val name: String,
    override val order: Int,
    override val objectives: List<ObjectiveConfig> = emptyList(),
    override val waves: List<WaveConfig> = emptyList(),
    override val startTriggers: List<TriggerConfig> = emptyList(),
    override val completeTriggers: List<TriggerConfig> = emptyList()
) : PhaseConfig

data class SimpleObjectiveConfig(
    override val objectiveId: String,
    override val type: ObjectiveType,
    override val description: String,
    override val targetCount: Int = 1,
    override val required: Boolean = true
) : ObjectiveConfig

data class SimpleWaveConfig(
    override val waveId: String,
    override val order: Int,
    override val mobs: List<MobSpawnConfig> = emptyList(),
    override val delayAfterPrevious: Int = 0,
    override val isBossWave: Boolean = false
) : WaveConfig

data class SimpleMobSpawnConfig(
    override val mobType: String,
    override val count: Int = 1,
    override val level: Int = 1,
    override val spawnPointTag: String = "",
    override val modifiers: Map<String, Double> = emptyMap()
) : MobSpawnConfig

data class SimpleTriggerConfig(
    override val type: String,
    override val parameters: Map<String, Any> = emptyMap()
) : TriggerConfig

data class SimpleRewardConfig(
    override val experience: Int = 0,
    override val gold: Int = 0,
    override val fixedItems: List<ItemReward> = emptyList(),
    override val randomItemPool: List<RandomItemReward> = emptyList(),
    override val firstClearBonus: List<ItemReward> = emptyList()
) : RewardConfig

data class SimpleItemReward(
    override val itemId: String,
    override val amount: Int = 1
) : ItemReward

data class SimpleRandomItemReward(
    override val itemId: String,
    override val amountRange: IntRange = 1..1,
    override val dropRate: Double = 1.0
) : RandomItemReward
