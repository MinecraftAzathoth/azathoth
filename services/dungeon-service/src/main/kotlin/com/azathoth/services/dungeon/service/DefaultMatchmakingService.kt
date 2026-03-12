package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.dungeon.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

private val logger = KotlinLogging.logger {}

class DefaultMatchmakingService(
    private val dungeonService: DefaultDungeonService
) : MatchmakingService {

    // key: "templateId:difficulty"
    private val queues = ConcurrentHashMap<String, ConcurrentLinkedQueue<MatchmakingRequest>>()
    private val playerQueue = ConcurrentHashMap<PlayerId, QueueEntry>()
    private val matchResults = ConcurrentHashMap<String, SimpleMatchmakingResult>()

    private data class QueueEntry(
        val queueKey: String,
        val matchId: String,
        val request: MatchmakingRequest
    )

    override suspend fun joinQueue(request: MatchmakingRequest): Result<MatchmakingResult> {
        if (playerQueue.containsKey(request.playerId)) {
            return Result.failure(ErrorCodes.ALREADY_EXISTS, "玩家已在匹配队列中")
        }

        val queueKey = "${request.templateId}:${request.difficulty}"
        val queue = queues.getOrPut(queueKey) { ConcurrentLinkedQueue() }
        queue.add(request)

        val matchId = UUID.randomUUID().toString()
        playerQueue[request.playerId] = QueueEntry(queueKey, matchId, request)

        // 尝试匹配
        val matched = tryMatch(queueKey)
        if (matched != null) {
            return Result.success(matched)
        }

        val result = SimpleMatchmakingResult(
            status = MatchmakingStatus.QUEUED,
            matchId = matchId,
            estimatedWaitTime = Duration.ofSeconds(30),
            message = "已加入匹配队列"
        )
        matchResults[matchId] = result
        logger.info { "玩家 ${request.playerId} 加入匹配队列: $queueKey" }
        return Result.success(result)
    }

    override suspend fun leaveQueue(playerId: PlayerId): Result<Unit> {
        val entry = playerQueue.remove(playerId)
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家不在匹配队列中")

        val queue = queues[entry.queueKey]
        queue?.removeIf { it.playerId == playerId }

        matchResults[entry.matchId] = SimpleMatchmakingResult(
            status = MatchmakingStatus.CANCELLED,
            matchId = entry.matchId,
            message = "已取消匹配"
        )
        logger.info { "玩家 $playerId 离开匹配队列" }
        return Result.success(Unit)
    }

    override suspend fun getQueueStatus(playerId: PlayerId): Result<MatchmakingResult> {
        val entry = playerQueue[playerId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家不在匹配队列中")

        val result = matchResults[entry.matchId] ?: SimpleMatchmakingResult(
            status = MatchmakingStatus.QUEUED,
            matchId = entry.matchId,
            estimatedWaitTime = Duration.ofSeconds(30)
        )
        return Result.success(result)
    }

    override suspend fun confirmMatch(playerId: PlayerId, matchId: String): Result<Unit> {
        val entry = playerQueue[playerId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家不在匹配队列中")

        if (entry.matchId != matchId) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "匹配ID不匹配")
        }

        logger.info { "玩家 $playerId 确认匹配 $matchId" }
        return Result.success(Unit)
    }

    override suspend fun declineMatch(playerId: PlayerId, matchId: String): Result<Unit> {
        val entry = playerQueue.remove(playerId)
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "玩家不在匹配队列中")

        matchResults[entry.matchId] = SimpleMatchmakingResult(
            status = MatchmakingStatus.CANCELLED,
            matchId = entry.matchId,
            message = "玩家拒绝匹配"
        )
        logger.info { "玩家 $playerId 拒绝匹配 $matchId" }
        return Result.success(Unit)
    }

    private suspend fun tryMatch(queueKey: String): SimpleMatchmakingResult? {
        val queue = queues[queueKey] ?: return null
        val parts = queueKey.split(":")
        if (parts.size != 2) return null

        val templateId = parts[0]
        val difficulty = DungeonDifficulty.valueOf(parts[1])

        val template = dungeonService.getTemplate(templateId).getOrNull() ?: return null
        if (queue.size < template.minPlayers) return null

        // 收集足够的玩家
        val matched = mutableListOf<MatchmakingRequest>()
        while (matched.size < template.maxPlayers && queue.isNotEmpty()) {
            val req = queue.poll() ?: break
            matched.add(req)
        }

        if (matched.size < template.minPlayers) {
            // 放回队列
            queue.addAll(matched)
            return null
        }

        // 创建副本实例
        val leader = matched.first().playerId
        val members = matched.drop(1).map { it.playerId }
        val instanceResult = dungeonService.createInstance(templateId, difficulty, leader, members)
        val instance = instanceResult.getOrNull() ?: return null

        val matchId = UUID.randomUUID().toString()
        val result = SimpleMatchmakingResult(
            status = MatchmakingStatus.FOUND,
            matchId = matchId,
            instanceId = instance.instanceId,
            message = "匹配成功"
        )

        // 更新所有匹配玩家的状态
        for (req in matched) {
            val entry = playerQueue.remove(req.playerId)
            if (entry != null) {
                matchResults[entry.matchId] = result
            }
        }

        logger.info { "匹配成功: $matchId -> 副本 ${instance.instanceId}" }
        return result
    }
}
