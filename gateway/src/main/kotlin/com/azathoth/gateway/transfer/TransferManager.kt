package com.azathoth.gateway.transfer

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import com.azathoth.gateway.routing.GameInstance
import com.azathoth.gateway.session.PlayerSession

/**
 * 传送状态
 */
enum class TransferState {
    /** 初始化 */
    INITIALIZING,
    /** 准备中（保存玩家数据） */
    PREPARING,
    /** 传送中 */
    TRANSFERRING,
    /** 连接目标实例中 */
    CONNECTING,
    /** 完成 */
    COMPLETED,
    /** 失败 */
    FAILED,
    /** 已取消 */
    CANCELLED
}

/**
 * 传送请求
 */
interface TransferRequest {
    /** 请求ID */
    val requestId: String
    
    /** 玩家会话 */
    val session: PlayerSession
    
    /** 源实例ID */
    val sourceInstanceId: InstanceId
    
    /** 目标实例 */
    val targetInstance: GameInstance
    
    /** 传送数据 */
    val transferData: TransferData
    
    /** 请求时间 */
    val requestTime: Long
    
    /** 超时时间（毫秒） */
    val timeoutMs: Long
}

/**
 * 传送数据
 */
interface TransferData {
    /** 玩家位置 */
    val position: Position?
    
    /** 传送原因 */
    val reason: String
    
    /** 附加数据 */
    val extras: Map<String, Any>
}

/**
 * 位置信息
 */
data class Position(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f,
    val world: String? = null
)

/**
 * 传送结果
 */
interface TransferResult {
    /** 请求ID */
    val requestId: String
    
    /** 是否成功 */
    val success: Boolean
    
    /** 传送耗时（毫秒） */
    val durationMs: Long
    
    /** 错误信息 */
    val error: String?
}

/**
 * 传送管理器
 */
interface TransferManager {
    /**
     * 发起传送请求
     */
    suspend fun transfer(request: TransferRequest): Result<TransferResult>
    
    /**
     * 快速传送（简化版）
     */
    suspend fun transfer(
        session: PlayerSession,
        targetInstance: GameInstance,
        reason: String = "transfer"
    ): Result<TransferResult>
    
    /**
     * 批量传送
     */
    suspend fun transferBatch(
        sessions: List<PlayerSession>,
        targetInstance: GameInstance,
        reason: String = "batch_transfer"
    ): Map<PlayerId, Result<TransferResult>>
    
    /**
     * 取消传送
     */
    suspend fun cancelTransfer(requestId: String): Boolean
    
    /**
     * 获取传送状态
     */
    fun getTransferState(requestId: String): TransferState?
    
    /**
     * 获取进行中的传送
     */
    fun getPendingTransfers(): List<TransferRequest>
    
    /**
     * 添加传送监听器
     */
    fun addListener(listener: TransferListener)
    
    /**
     * 移除传送监听器
     */
    fun removeListener(listener: TransferListener)
}

/**
 * 传送监听器
 */
interface TransferListener {
    /** 传送开始 */
    suspend fun onTransferStart(request: TransferRequest)
    
    /** 传送状态变更 */
    suspend fun onStateChange(request: TransferRequest, oldState: TransferState, newState: TransferState)
    
    /** 传送完成 */
    suspend fun onTransferComplete(request: TransferRequest, result: TransferResult)
    
    /** 传送失败 */
    suspend fun onTransferFailed(request: TransferRequest, error: String)
}
