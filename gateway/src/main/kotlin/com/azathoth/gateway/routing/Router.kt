package com.azathoth.gateway.routing

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.core.common.identity.PlayerId
import com.azathoth.gateway.session.PlayerSession

/**
 * 实例类型
 */
enum class InstanceType {
    /** 大厅 */
    LOBBY,
    /** 世界 */
    WORLD,
    /** 副本 */
    DUNGEON,
    /** 竞技场 */
    ARENA,
    /** 活动 */
    EVENT
}

/**
 * 游戏实例信息
 */
interface GameInstance {
    /** 实例ID */
    val instanceId: InstanceId
    
    /** 实例类型 */
    val type: InstanceType
    
    /** 实例名称 */
    val name: String
    
    /** 主机地址 */
    val host: String
    
    /** 端口 */
    val port: Int
    
    /** 当前玩家数 */
    val currentPlayers: Int
    
    /** 最大玩家数 */
    val maxPlayers: Int
    
    /** 是否接受新玩家 */
    val acceptingPlayers: Boolean
    
    /** 实例元数据 */
    val metadata: Map<String, String>
    
    /** 是否已满 */
    val isFull: Boolean get() = currentPlayers >= maxPlayers
    
    /** 可用容量 */
    val availableCapacity: Int get() = maxPlayers - currentPlayers
}

/**
 * 路由请求
 */
interface RouteRequest {
    /** 会话 */
    val session: PlayerSession
    
    /** 目标实例类型 */
    val targetType: InstanceType?
    
    /** 目标实例ID（指定实例时） */
    val targetInstanceId: InstanceId?
    
    /** 路由参数 */
    val parameters: Map<String, String>
}

/**
 * 路由结果
 */
interface RouteResult {
    /** 是否成功 */
    val success: Boolean
    
    /** 目标实例 */
    val targetInstance: GameInstance?
    
    /** 错误信息 */
    val error: String?
}

/**
 * 路由器
 */
interface Router {
    /**
     * 路由玩家到合适的实例
     */
    suspend fun route(request: RouteRequest): RouteResult
    
    /**
     * 获取可用实例列表
     */
    suspend fun getAvailableInstances(type: InstanceType): List<GameInstance>
    
    /**
     * 获取实例信息
     */
    suspend fun getInstance(instanceId: InstanceId): GameInstance?
    
    /**
     * 添加路由规则
     */
    fun addRule(rule: RouteRule)
    
    /**
     * 移除路由规则
     */
    fun removeRule(ruleName: String)
}

/**
 * 路由规则
 */
interface RouteRule {
    /** 规则名称 */
    val name: String
    
    /** 优先级 */
    val priority: Int
    
    /** 是否匹配请求 */
    fun matches(request: RouteRequest): Boolean
    
    /** 执行路由 */
    suspend fun route(request: RouteRequest, instances: List<GameInstance>): GameInstance?
}

/**
 * 实例注册表
 */
interface InstanceRegistry {
    /** 注册实例 */
    suspend fun register(instance: GameInstance)
    
    /** 注销实例 */
    suspend fun unregister(instanceId: InstanceId)
    
    /** 更新实例信息 */
    suspend fun update(instance: GameInstance)
    
    /** 获取实例 */
    fun getInstance(instanceId: InstanceId): GameInstance?
    
    /** 获取所有实例 */
    fun getAllInstances(): Collection<GameInstance>
    
    /** 获取指定类型的实例 */
    fun getInstancesByType(type: InstanceType): Collection<GameInstance>
    
    /** 实例心跳 */
    suspend fun heartbeat(instanceId: InstanceId, currentPlayers: Int)
}
