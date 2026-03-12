package com.azathoth.services.dungeon.model

import com.azathoth.core.common.identity.PlayerId
import java.time.Duration
import java.time.Instant

data class SimpleDungeonTemplateInfo(
    override val templateId: String,
    override val name: String,
    override val description: String,
    override val minPlayers: Int,
    override val maxPlayers: Int,
    override val recommendedLevel: Int,
    override val minLevel: Int,
    override val supportedDifficulties: List<DungeonDifficulty>,
    override val timeLimit: Duration,
    override val dailyEntryLimit: Int,
    override val weeklyEntryLimit: Int
) : DungeonTemplateInfo

data class SimpleDungeonInstanceInfo(
    override val instanceId: String,
    override val templateId: String,
    override val difficulty: DungeonDifficulty,
    override val state: DungeonInstanceState,
    override val playerIds: Set<PlayerId>,
    override val leaderId: PlayerId,
    override val createdAt: Instant,
    override val startedAt: Instant? = null,
    override val completedAt: Instant? = null,
    override val elapsedTime: Duration,
    override val remainingTime: Duration? = null,
    override val currentPhase: Int,
    override val totalPhases: Int
) : DungeonInstanceInfo

data class SimpleJoinResult(
    override val success: Boolean,
    override val instanceId: String,
    override val worldName: String,
    override val spawnPosition: SpawnPosition
) : JoinResult

data class SimpleSpawnPosition(
    override val x: Double,
    override val y: Double,
    override val z: Double
) : SpawnPosition

data class SimpleDungeonProgress(
    override val instanceId: String,
    override val currentPhase: Int,
    override val totalPhases: Int,
    override val monstersKilled: Int,
    override val bossesKilled: Int,
    override val deaths: Int,
    override val score: Int,
    override val objectives: List<ObjectiveProgress>
) : DungeonProgress

data class SimpleObjectiveProgress(
    override val objectiveId: String,
    override val name: String,
    override val description: String,
    override val current: Int,
    override val target: Int,
    override val completed: Boolean
) : ObjectiveProgress

data class SimpleDungeonResult(
    override val instanceId: String,
    override val templateId: String,
    override val difficulty: DungeonDifficulty,
    override val success: Boolean,
    override val rating: DungeonRating,
    override val duration: Duration,
    override val score: Int,
    override val participants: List<ParticipantResult>,
    override val completedAt: Instant
) : DungeonResult

data class SimpleParticipantResult(
    override val playerId: PlayerId,
    override val damage: Long,
    override val healing: Long,
    override val deaths: Int,
    override val contribution: Double,
    override val rewards: List<RewardItem>
) : ParticipantResult

data class SimpleRewardItem(
    override val itemId: String,
    override val amount: Int,
    override val metadata: Map<String, Any> = emptyMap()
) : RewardItem

data class SimpleDungeonRecord(
    override val recordId: String,
    override val playerId: PlayerId,
    override val templateId: String,
    override val difficulty: DungeonDifficulty,
    override val cleared: Boolean,
    override val duration: Duration,
    override val score: Int,
    override val rating: DungeonRating,
    override val completedAt: Instant
) : DungeonRecord

data class SimpleLeaderboardEntry(
    override val rank: Int,
    override val playerId: PlayerId,
    override val playerName: String,
    override val score: Int,
    override val duration: Duration,
    override val rating: DungeonRating,
    override val achievedAt: Instant
) : LeaderboardEntry

data class SimpleEntryCheckResult(
    override val canEnter: Boolean,
    override val usedEntries: Int,
    override val maxEntries: Int,
    override val resetAt: Instant
) : EntryCheckResult

data class SimpleMatchmakingRequest(
    override val playerId: PlayerId,
    override val templateId: String,
    override val difficulty: DungeonDifficulty,
    override val partyIds: List<PlayerId> = emptyList(),
    override val preferences: MatchmakingPreferences
) : MatchmakingRequest

data class SimpleMatchmakingPreferences(
    override val maxWaitTime: Duration = Duration.ofMinutes(5),
    override val acceptCrossServer: Boolean = true,
    override val preferSameLanguage: Boolean = false
) : MatchmakingPreferences

data class SimpleMatchmakingResult(
    override val status: MatchmakingStatus,
    override val matchId: String? = null,
    override val estimatedWaitTime: Duration? = null,
    override val instanceId: String? = null,
    override val message: String? = null
) : MatchmakingResult
