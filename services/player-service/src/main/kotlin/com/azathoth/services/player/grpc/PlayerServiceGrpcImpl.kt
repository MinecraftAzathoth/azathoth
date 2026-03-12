package com.azathoth.services.player.grpc

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.core.grpc.player.*
import com.azathoth.services.player.service.PlayerService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * PlayerService gRPC 实现
 *
 * 桥接 proto 生成的 gRPC stub 到业务层 [PlayerService]。
 */
class PlayerServiceGrpcImpl(
    private val playerService: PlayerService
) : PlayerServiceGrpcKt.PlayerServiceCoroutineImplBase() {

    override suspend fun getPlayer(request: GetPlayerRequest): PlayerResponse {
        logger.debug { "gRPC getPlayer: ${request.playerId}" }

        val result = playerService.getPlayer(PlayerId(request.playerId))
        return when (result) {
            is Result.Success -> playerResponse {
                success = true
                player = result.value.toPlayerData()
            }
            is Result.Failure -> playerResponse {
                success = false
                errorMessage = result.error.message
            }
        }
    }

    override suspend fun updatePlayer(request: UpdatePlayerRequest): PlayerResponse {
        logger.debug { "gRPC updatePlayer: ${request.playerId}" }

        val existing = playerService.getPlayer(PlayerId(request.playerId))
        if (existing is Result.Failure) {
            return playerResponse {
                success = false
                errorMessage = existing.error.message
            }
        }

        val entity = (existing as Result.Success).value
        // 从 proto PlayerData 更新实体字段
        request.data?.let { data ->
            if (data.name.isNotBlank()) entity.displayName = data.name
            if (data.level > 0) entity.level = data.level
            if (data.experience > 0) entity.experience = data.experience
        }

        val updated = playerService.updatePlayer(entity)
        return when (updated) {
            is Result.Success -> playerResponse {
                success = true
                player = updated.value.toPlayerData()
            }
            is Result.Failure -> playerResponse {
                success = false
                errorMessage = updated.error.message
            }
        }
    }

    override suspend fun playerJoin(request: PlayerJoinRequest): PlayerJoinResponse {
        logger.info { "gRPC playerJoin: ${request.playerId} -> instance=${request.instanceId}" }

        return try {
            playerService.onPlayerJoin(PlayerId(request.playerId))
            playerJoinResponse { success = true }
        } catch (e: Exception) {
            logger.error(e) { "playerJoin 失败: ${request.playerId}" }
            playerJoinResponse {
                success = false
                errorMessage = e.message ?: "未知错误"
            }
        }
    }

    override suspend fun playerLeave(request: PlayerLeaveRequest): PlayerLeaveResponse {
        logger.info { "gRPC playerLeave: ${request.playerId}, reason=${request.reason}" }

        return try {
            playerService.onPlayerLeave(PlayerId(request.playerId))
            playerLeaveResponse { success = true }
        } catch (e: Exception) {
            logger.error(e) { "playerLeave 失败: ${request.playerId}" }
            playerLeaveResponse { success = false }
        }
    }
}

/**
 * PlayerEntity → proto PlayerData 转换
 */
private fun com.azathoth.services.player.repository.PlayerEntity.toPlayerData(): PlayerData =
    playerData {
        id = playerId.value
        name = displayName
        lastLogin = lastLoginAt
        level = this@toPlayerData.level
        experience = this@toPlayerData.experience
        metadata.putAll(
            buildMap {
                put("gold", gold.toString())
                put("diamond", diamond.toString())
                put("vipLevel", vipLevel.toString())
                if (isBanned) put("banned", "true")
            }
        )
    }
