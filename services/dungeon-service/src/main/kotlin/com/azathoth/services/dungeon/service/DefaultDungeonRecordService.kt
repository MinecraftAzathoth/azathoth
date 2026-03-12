package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.dungeon.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class DefaultDungeonRecordService : DungeonRecordService {

    private val records = ConcurrentHashMap<String, MutableList<SimpleDungeonRecord>>()
    // key: "templateId:difficulty:type"
    private val leaderboards = ConcurrentHashMap<String, MutableList<SimpleLeaderboardEntry>>()
    // key: "playerId:templateId:date"
    private val dailyEntries = ConcurrentHashMap<String, Int>()

    fun addRecord(result: DungeonResult) {
        for (participant in result.participants) {
            val record = SimpleDungeonRecord(
                recordId = UUID.randomUUID().toString(),
                playerId = participant.playerId,
                templateId = result.templateId,
                difficulty = result.difficulty,
                cleared = result.success,
                duration = result.duration,
                score = result.score,
                rating = result.rating,
                completedAt = result.completedAt
            )
            records.getOrPut(participant.playerId.value) { mutableListOf() }.add(record)

            // 更新每日入场次数
            val dailyKey = "${participant.playerId.value}:${result.templateId}:${LocalDate.now()}"
            dailyEntries.merge(dailyKey, 1, Int::plus)

            // 更新排行榜
            if (result.success) {
                updateLeaderboard(result, participant)
            }
        }
        logger.info { "记录副本结果: ${result.instanceId}" }
    }

    private fun updateLeaderboard(result: DungeonResult, participant: ParticipantResult) {
        val scoreKey = "${result.templateId}:${result.difficulty}:${LeaderboardType.HIGHEST_SCORE}"
        val speedKey = "${result.templateId}:${result.difficulty}:${LeaderboardType.FASTEST_CLEAR}"

        val entry = SimpleLeaderboardEntry(
            rank = 0,
            playerId = participant.playerId,
            playerName = participant.playerId.value,
            score = result.score,
            duration = result.duration,
            rating = result.rating,
            achievedAt = result.completedAt
        )

        for (key in listOf(scoreKey, speedKey)) {
            leaderboards.getOrPut(key) { mutableListOf() }.add(entry)
        }
    }

    override suspend fun getPlayerRecords(
        playerId: PlayerId,
        templateId: String?,
        limit: Int
    ): Result<List<DungeonRecord>> {
        val playerRecords = records[playerId.value] ?: emptyList()
        val filtered = playerRecords
            .filter { templateId == null || it.templateId == templateId }
            .sortedByDescending { it.completedAt }
            .take(limit)
        return Result.success(filtered)
    }

    override suspend fun getLeaderboard(
        templateId: String,
        difficulty: DungeonDifficulty,
        type: LeaderboardType,
        limit: Int
    ): Result<List<LeaderboardEntry>> {
        val key = "$templateId:$difficulty:$type"
        val entries = leaderboards[key] ?: emptyList()

        val sorted = when (type) {
            LeaderboardType.HIGHEST_SCORE -> entries.sortedByDescending { it.score }
            LeaderboardType.FASTEST_CLEAR -> entries.sortedBy { it.duration }
            LeaderboardType.MOST_CLEARS -> entries.sortedByDescending { it.score }
        }

        val ranked = sorted.take(limit).mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }
        return Result.success(ranked)
    }

    override suspend fun getPlayerRank(
        playerId: PlayerId,
        templateId: String,
        difficulty: DungeonDifficulty,
        type: LeaderboardType
    ): Result<LeaderboardEntry?> {
        val leaderboard = getLeaderboard(templateId, difficulty, type).getOrNull() ?: emptyList()
        val entry = leaderboard.find { it.playerId == playerId }
        return Result.success(entry)
    }

    override suspend fun checkDailyEntries(
        playerId: PlayerId,
        templateId: String
    ): Result<EntryCheckResult> {
        val dailyKey = "${playerId.value}:$templateId:${LocalDate.now()}"
        val used = dailyEntries[dailyKey] ?: 0
        val maxEntries = 3 // 默认每日3次

        val result = SimpleEntryCheckResult(
            canEnter = used < maxEntries,
            usedEntries = used,
            maxEntries = maxEntries,
            resetAt = Instant.now().plusSeconds(86400)
        )
        return Result.success(result)
    }
}
