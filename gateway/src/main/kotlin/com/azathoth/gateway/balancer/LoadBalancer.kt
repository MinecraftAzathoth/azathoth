package com.azathoth.gateway.balancer

import com.azathoth.gateway.routing.GameInstance
import com.azathoth.gateway.session.PlayerSession

/**
 * 负载均衡策略
 */
enum class BalancingStrategy {
    /** 轮询 */
    ROUND_ROBIN,
    /** 最少连接 */
    LEAST_CONNECTIONS,
    /** 加权轮询 */
    WEIGHTED_ROUND_ROBIN,
    /** 加权最少连接 */
    WEIGHTED_LEAST_CONNECTIONS,
    /** 随机 */
    RANDOM,
    /** 一致性哈希 */
    CONSISTENT_HASH,
    /** 响应时间加权 */
    RESPONSE_TIME_WEIGHTED
}

/**
 * 实例权重
 */
interface InstanceWeight {
    /** 实例 */
    val instance: GameInstance
    
    /** 静态权重 */
    val staticWeight: Int
    
    /** 动态权重（根据负载调整） */
    val dynamicWeight: Int
    
    /** 有效权重 */
    val effectiveWeight: Int get() = staticWeight * dynamicWeight / 100
}

/**
 * 负载均衡器
 */
interface GatewayLoadBalancer {
    /** 均衡策略 */
    val strategy: BalancingStrategy
    
    /**
     * 选择实例
     */
    fun select(instances: List<GameInstance>, session: PlayerSession? = null): GameInstance?
    
    /**
     * 选择实例（带权重）
     */
    fun selectWeighted(instances: List<InstanceWeight>, session: PlayerSession? = null): GameInstance?
    
    /**
     * 更新实例统计
     */
    fun updateStats(instance: GameInstance, responseTimeMs: Long, success: Boolean)
    
    /**
     * 重置统计
     */
    fun resetStats()
}

/**
 * 健康检查器
 */
interface HealthChecker {
    /**
     * 检查实例健康状态
     */
    suspend fun check(instance: GameInstance): HealthStatus
    
    /**
     * 批量检查
     */
    suspend fun checkAll(instances: List<GameInstance>): Map<GameInstance, HealthStatus>
    
    /**
     * 启动定期健康检查
     */
    fun startPeriodicCheck(intervalMs: Long)
    
    /**
     * 停止定期健康检查
     */
    fun stopPeriodicCheck()
    
    /**
     * 添加健康状态监听器
     */
    fun addListener(listener: HealthCheckListener)
}

/**
 * 健康状态
 */
interface HealthStatus {
    /** 实例 */
    val instance: GameInstance
    
    /** 是否健康 */
    val healthy: Boolean
    
    /** 响应时间（毫秒） */
    val responseTimeMs: Long
    
    /** 检查时间 */
    val checkTime: Long
    
    /** 错误信息 */
    val error: String?
    
    /** 连续失败次数 */
    val consecutiveFailures: Int
}

/**
 * 健康检查监听器
 */
interface HealthCheckListener {
    /** 健康状态变更 */
    suspend fun onHealthChange(instance: GameInstance, oldStatus: HealthStatus?, newStatus: HealthStatus)
    
    /** 实例变为不健康 */
    suspend fun onUnhealthy(instance: GameInstance, status: HealthStatus)
    
    /** 实例恢复健康 */
    suspend fun onRecovered(instance: GameInstance, status: HealthStatus)
}
