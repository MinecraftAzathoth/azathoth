package com.azathoth.services.dungeon.service

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.services.dungeon.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class DefaultDungeonService : DungeonService {

    private val templates = ConcurrentHashMap<String, SimpleDungeonTemplateInfo>()
    private val instances = ConcurrentHashMap<String, SimpleDungeonInstanceInfo>()
    private val progressMap = ConcurrentHashMap<String, SimpleDungeonProgress>()

    // 用于记录服务的回调
    var onInstanceCompleted: (suspend (DungeonResult) -> Unit)? = null

    fun registerTemplate(template: SimpleDungeonTemplateInfo) {
        templates[template.templateId] = template
        logger.info { "注册副本模板: ${template.name} (${template.templateId})" }
    }

    override suspend fun listTemplates(
        minLevel: Int?,
        maxLevel: Int?,
        difficulty: DungeonDifficulty?
    ): Result<List<DungeonTemplateInfo>> {
        val filtered = templates.values.filter { t ->
            (minLevel == null || t.recommendedLevel >= minLevel) &&
                (maxLevel == null || t.recommendedLevel <= maxLevel) &&
                (difficulty == null || difficulty in t.supportedDifficulties)
        }
        return Result.success(filtered)
    }

    override suspend fun getTemplate(templateId: String): Result<DungeonTemplateInfo> {
        val template = templates[templateId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "副本模板不存在: $templateId")
        return Result.success(template)
    }

    override suspend fun createInstance(
        templateId: String,
        difficulty: DungeonDifficulty,
        leaderId: PlayerId,
        memberIds: List<PlayerId>
    ): Result<DungeonInstanceInfo> {
        val template = templates[templateId]
            ?: return Result.failure(ErrorCodes.NOT_FOUND, "副本模板不存在: $templateId")

        if (difficulty !in template.supportedDifficulties) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "不支持的难度: $difficulty")
        }

        val allPlayers = (memberIds + leaderId).toSet()
        if (allPlayers.size > template.maxPlayers) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "玩家数量超过上限: ${template.maxPlayers}")
        }

        val instanceId = UUID.randomUUID().toString()
        val now = Instant.now()
        val instance = SimpleDungeonInstanceInfo(
            instanceId = instanceId,
            templateId = templateId,
            difficulty = difficulty,
            state = DungeonInstanceState.WAITING,
            playerIds = allPlayers,
            leaderId = leaderId,
            createdAt = now,
            elapsedTime = Duration.ZERO,
            currentPhase = 0,
            totalPhases = 3
        )
        instances[instanceId] = instance

        progressMap[instanceId] = SimpleDungeonProgress(
            instanceId = instanceId,
            currentPhase = 0,
            totalPhases = 3,
            monstersKilled = 0,
            bossesKilled = 0,
            deaths = 0,
            score = 0,
            objectives = emptyList()
        )

        logger.info { "创建副本实例: $instanceId (模板: $templateId, 难度: $difficulty)" }
        return Result.success(instance)
    }

    override suspend fun getInstance(instanceId: String): Result<DungeonInstanceInfo> {
        val instance = instances[instanceId]
            ?: return Result.failure(ErrorCodes.INSTANCE_NOT_FOUND, "副本实例不存在: $instanceId")
        return Result.success(instance)
    }

    override suspend fun joinInstance(instanceId: String, playerId: PlayerId): Result<JoinResult> {
        val instance = instances[instanceId]
            ?: return Result.failure(ErrorCodes.INSTANCE_NOT_FOUND, "副本实例不存在: $instanceId")

        if (instance.state != DungeonInstanceState.WAITING) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "副本不在等待状态")
        }

        val updated = instance.copy(playerIds = instance.playerIds + playerId)
        instances[instanceId] = updated

        val result = SimpleJoinResult(
            success = true,
            instanceId = instanceId,
            worldName = "dungeon_${instance.templateId}_$instanceId",
            spawnPosition = SimpleSpawnPosition(0.0, 64.0, 0.0)
        )
        logger.info { "玩家 $playerId 加入副本 $instanceId" }
        return Result.success(result)
    }

    override suspend fun leaveInstance(instanceId: String, playerId: PlayerId): Result<Unit> {
        val instance = instances[instanceId]
            ?: return Result.failure(ErrorCodes.INSTANCE_NOT_FOUND, "副本实例不存在: $instanceId")

        if (playerId !in instance.playerIds) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "玩家不在副本中")
        }

        val remaining = instance.playerIds - playerId
        if (remaining.isEmpty()) {
            instances[instanceId] = instance.copy(
                state = DungeonInstanceState.CLOSED,
                playerIds = remaining
            )
        } else {
            val newLeader = if (instance.leaderId == playerId) remaining.first() else instance.leaderId
            instances[instanceId] = instance.copy(
                playerIds = remaining,
                leaderId = newLeader
            )
        }
        logger.info { "玩家 $playerId 离开副本 $instanceId" }
        return Result.success(Unit)
    }

    override suspend fun getProgress(instanceId: String): Result<DungeonProgress> {
        val progress = progressMap[instanceId]
            ?: return Result.failure(ErrorCodes.INSTANCE_NOT_FOUND, "副本进度不存在: $instanceId")
        return Result.success(progress)
    }

    override suspend fun completeInstance(instanceId: String, result: DungeonResult): Result<Unit> {
        val instance = instances[instanceId]
            ?: return Result.failure(ErrorCodes.INSTANCE_NOT_FOUND, "副本实例不存在: $instanceId")

        val newState = if (result.success) DungeonInstanceState.COMPLETED else DungeonInstanceState.FAILED
        instances[instanceId] = instance.copy(
            state = newState,
            completedAt = result.completedAt
        )

        onInstanceCompleted?.invoke(result)
        logger.info { "副本 $instanceId 完成, 结果: $newState" }
        return Result.success(Unit)
    }

    /**
     * 启动副本（状态转换: WAITING -> IN_PROGRESS）
     */
    fun startInstance(instanceId: String): Result<DungeonInstanceInfo> {
        val instance = instances[instanceId]
            ?: return Result.failure(ErrorCodes.INSTANCE_NOT_FOUND, "副本实例不存在: $instanceId")

        if (instance.state != DungeonInstanceState.WAITING) {
            return Result.failure(ErrorCodes.INVALID_ARGUMENT, "副本不在等待状态")
        }

        val updated = instance.copy(
            state = DungeonInstanceState.IN_PROGRESS,
            startedAt = Instant.now()
        )
        instances[instanceId] = updated
        logger.info { "副本 $instanceId 开始" }
        return Result.success(updated)
    }
}
