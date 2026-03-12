package com.azathoth.gateway.grpc

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.grpc.gateway.*
import com.azathoth.gateway.routing.InstanceRegistry
import com.azathoth.gateway.routing.InstanceType
import com.azathoth.gateway.routing.SimpleGameInstance
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * GatewayService gRPC 实现
 *
 * 接收来自 Game Instance 的注册、心跳、传送请求。
 */
class GatewayServiceGrpcImpl(
    private val instanceRegistry: InstanceRegistry
) : GatewayServiceGrpcKt.GatewayServiceCoroutineImplBase() {

    override suspend fun registerInstance(request: RegisterInstanceRequest): RegisterInstanceResponse {
        logger.info { "gRPC registerInstance: ${request.instanceId} (${request.instanceType})" }

        return try {
            val instanceType = try {
                InstanceType.valueOf(request.instanceType.uppercase())
            } catch (_: IllegalArgumentException) {
                InstanceType.WORLD
            }

            val instance = SimpleGameInstance(
                instanceId = InstanceId(request.instanceId),
                type = instanceType,
                name = request.instanceId,
                host = request.host,
                port = request.port,
                maxPlayers = request.maxPlayers,
                metadata = request.metadataMap
            )

            instanceRegistry.register(instance)
            registerInstanceResponse { success = true }
        } catch (e: Exception) {
            logger.error(e) { "registerInstance 失败: ${request.instanceId}" }
            registerInstanceResponse {
                success = false
                errorMessage = e.message ?: "注册失败"
            }
        }
    }

    override suspend fun unregisterInstance(request: UnregisterInstanceRequest): UnregisterInstanceResponse {
        logger.info { "gRPC unregisterInstance: ${request.instanceId}" }

        return try {
            instanceRegistry.unregister(InstanceId(request.instanceId))
            unregisterInstanceResponse { success = true }
        } catch (e: Exception) {
            logger.error(e) { "unregisterInstance 失败: ${request.instanceId}" }
            unregisterInstanceResponse { success = false }
        }
    }

    override suspend fun heartbeat(request: HeartbeatRequest): HeartbeatResponse {
        logger.debug { "gRPC heartbeat: ${request.instanceId}, players=${request.currentPlayers}" }

        return try {
            instanceRegistry.heartbeat(
                InstanceId(request.instanceId),
                request.currentPlayers
            )
            heartbeatResponse { acknowledged = true }
        } catch (e: Exception) {
            logger.error(e) { "heartbeat 失败: ${request.instanceId}" }
            heartbeatResponse { acknowledged = false }
        }
    }

    override suspend fun transferPlayer(request: TransferPlayerRequest): TransferPlayerResponse {
        logger.info { "gRPC transferPlayer: ${request.playerId} from ${request.fromInstance} to ${request.toInstance}" }

        val targetInstance = instanceRegistry.getInstance(InstanceId(request.toInstance))
        if (targetInstance == null) {
            return transferPlayerResponse {
                success = false
                errorMessage = "目标实例不存在: ${request.toInstance}"
            }
        }

        if (targetInstance.isFull) {
            return transferPlayerResponse {
                success = false
                errorMessage = "目标实例已满: ${request.toInstance}"
            }
        }

        // 传送逻辑由 TransferManager 处理，这里只做基本验证
        return transferPlayerResponse { success = true }
    }
}
