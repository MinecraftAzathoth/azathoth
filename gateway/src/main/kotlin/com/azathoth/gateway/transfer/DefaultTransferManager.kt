package com.azathoth.gateway.transfer

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import com.azathoth.gateway.routing.GameInstance
import com.azathoth.gateway.session.PlayerSession
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

// --- Data Classes ---

data class SimpleTransferData(
    override val position: Position? = null,
    override val reason: String = "transfer",
    override val extras: Map<String, Any> = emptyMap()
) : TransferData

data class SimpleTransferRequest(
    override val requestId: String = UUID.randomUUID().toString(),
    override val session: PlayerSession,
    override val sourceInstanceId: InstanceId,
    override val targetInstance: GameInstance,
    override val transferData: TransferData,
    override val requestTime: Long = System.currentTimeMillis(),
    override val timeoutMs: Long = 30_000L
) : TransferRequest

data class SimpleTransferResult(
    override val requestId: String,
    override val success: Boolean,
    override val durationMs: Long,
    override val error: String? = null
) : TransferResult

// --- DefaultTransferManager ---

class DefaultTransferManager : TransferManager {

    private val transfers = ConcurrentHashMap<String, TransferRequest>()
    private val states = ConcurrentHashMap<String, TransferState>()
    private val listeners = ConcurrentHashMap.newKeySet<TransferListener>()

    override suspend fun transfer(request: TransferRequest): Result<TransferResult> {
        val requestId = request.requestId
        transfers[requestId] = request
        val startTime = System.currentTimeMillis()

        try {
            // INITIALIZING
            transitionState(requestId, request, TransferState.INITIALIZING)
            listeners.forEach { it.onTransferStart(request) }

            // PREPARING
            transitionState(requestId, request, TransferState.PREPARING)
            delay(10) // 模拟数据保存

            // 检查超时
            if (System.currentTimeMillis() - startTime > request.timeoutMs) {
                return failTransfer(requestId, request, startTime, "传送超时")
            }

            // 检查取消
            if (states[requestId] == TransferState.CANCELLED) {
                return failTransfer(requestId, request, startTime, "传送已取消")
            }

            // TRANSFERRING
            transitionState(requestId, request, TransferState.TRANSFERRING)

            // CONNECTING
            transitionState(requestId, request, TransferState.CONNECTING)

            // 检查目标实例可用性
            if (!request.targetInstance.acceptingPlayers || request.targetInstance.isFull) {
                return failTransfer(requestId, request, startTime, "目标实例不可用")
            }

            // COMPLETED
            transitionState(requestId, request, TransferState.COMPLETED)
            val result = SimpleTransferResult(
                requestId = requestId,
                success = true,
                durationMs = System.currentTimeMillis() - startTime
            )
            listeners.forEach { it.onTransferComplete(request, result) }
            transfers.remove(requestId)
            return Result.success(result)

        } catch (e: Exception) {
            return failTransfer(requestId, request, startTime, e.message ?: "未知错误")
        }
    }

    override suspend fun transfer(
        session: PlayerSession,
        targetInstance: GameInstance,
        reason: String
    ): Result<TransferResult> {
        val request = SimpleTransferRequest(
            session = session,
            sourceInstanceId = session.currentInstanceId ?: InstanceId.of("unknown"),
            targetInstance = targetInstance,
            transferData = SimpleTransferData(reason = reason)
        )
        return transfer(request)
    }

    override suspend fun transferBatch(
        sessions: List<PlayerSession>,
        targetInstance: GameInstance,
        reason: String
    ): Map<PlayerId, Result<TransferResult>> {
        val results = ConcurrentHashMap<PlayerId, Result<TransferResult>>()
        for (session in sessions) {
            results[session.playerId] = transfer(session, targetInstance, reason)
        }
        return results
    }

    override suspend fun cancelTransfer(requestId: String): Boolean {
        val current = states[requestId] ?: return false
        if (current == TransferState.COMPLETED || current == TransferState.FAILED || current == TransferState.CANCELLED) {
            return false
        }
        val request = transfers[requestId]
        states[requestId] = TransferState.CANCELLED
        if (request != null) {
            listeners.forEach { it.onTransferFailed(request, "传送已取消") }
        }
        transfers.remove(requestId)
        logger.info { "传送已取消: $requestId" }
        return true
    }

    override fun getTransferState(requestId: String): TransferState? = states[requestId]

    override fun getPendingTransfers(): List<TransferRequest> = transfers.values.toList()

    override fun addListener(listener: TransferListener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: TransferListener) {
        listeners.remove(listener)
    }

    private suspend fun transitionState(
        requestId: String,
        request: TransferRequest,
        newState: TransferState
    ) {
        val oldState = states[requestId]
        states[requestId] = newState
        logger.debug { "传送状态变更: $requestId ${oldState ?: "null"} -> $newState" }
        if (oldState != null) {
            listeners.forEach { it.onStateChange(request, oldState, newState) }
        }
    }

    private suspend fun failTransfer(
        requestId: String,
        request: TransferRequest,
        startTime: Long,
        error: String
    ): Result<TransferResult> {
        transitionState(requestId, request, TransferState.FAILED)
        val result = SimpleTransferResult(
            requestId = requestId,
            success = false,
            durationMs = System.currentTimeMillis() - startTime,
            error = error
        )
        listeners.forEach { it.onTransferFailed(request, error) }
        transfers.remove(requestId)
        return Result.failure(ErrorCodes.TRANSFER_FAILED, error)
    }
}
