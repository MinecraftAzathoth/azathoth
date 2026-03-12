package com.azathoth.gateway.routing

import com.azathoth.core.common.identity.InstanceId
import com.azathoth.gateway.balancer.GatewayLoadBalancer
import com.azathoth.gateway.session.PlayerSession
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val logger = KotlinLogging.logger {}

// --- SimpleGameInstance ---

data class SimpleGameInstance(
    override val instanceId: InstanceId,
    override val type: InstanceType,
    override val name: String,
    override val host: String,
    override val port: Int,
    override val currentPlayers: Int = 0,
    override val maxPlayers: Int = 100,
    override val acceptingPlayers: Boolean = true,
    override val metadata: Map<String, String> = emptyMap()
) : GameInstance

// --- Simple RouteRequest / RouteResult ---

data class SimpleRouteRequest(
    override val session: PlayerSession,
    override val targetType: InstanceType? = null,
    override val targetInstanceId: InstanceId? = null,
    override val parameters: Map<String, String> = emptyMap()
) : RouteRequest

data class SimpleRouteResult(
    override val success: Boolean,
    override val targetInstance: GameInstance? = null,
    override val error: String? = null
) : RouteResult

// --- DefaultRouteRule ---

class DefaultRouteRule(
    override val name: String,
    override val priority: Int,
    private val targetType: InstanceType
) : RouteRule {

    override fun matches(request: RouteRequest): Boolean =
        request.targetType == targetType

    override suspend fun route(request: RouteRequest, instances: List<GameInstance>): GameInstance? =
        instances.filter { it.type == targetType && it.acceptingPlayers && !it.isFull }
            .minByOrNull { it.currentPlayers }
}

// --- DefaultInstanceRegistry ---

class DefaultInstanceRegistry : InstanceRegistry {
    private val instances = ConcurrentHashMap<InstanceId, SimpleGameInstance>()
    private val heartbeats = ConcurrentHashMap<InstanceId, Long>()

    override suspend fun register(instance: GameInstance) {
        val simple = if (instance is SimpleGameInstance) instance
        else SimpleGameInstance(
            instanceId = instance.instanceId, type = instance.type, name = instance.name,
            host = instance.host, port = instance.port, currentPlayers = instance.currentPlayers,
            maxPlayers = instance.maxPlayers, acceptingPlayers = instance.acceptingPlayers,
            metadata = instance.metadata
        )
        instances[instance.instanceId] = simple
        heartbeats[instance.instanceId] = System.currentTimeMillis()
        logger.info { "实例已注册: ${instance.instanceId.value} (${instance.name})" }
    }

    override suspend fun unregister(instanceId: InstanceId) {
        instances.remove(instanceId)
        heartbeats.remove(instanceId)
        logger.info { "实例已注销: ${instanceId.value}" }
    }

    override suspend fun update(instance: GameInstance) {
        val existing = instances[instance.instanceId] ?: return
        instances[instance.instanceId] = existing.copy(
            currentPlayers = instance.currentPlayers,
            maxPlayers = instance.maxPlayers,
            acceptingPlayers = instance.acceptingPlayers,
            metadata = instance.metadata
        )
    }

    override fun getInstance(instanceId: InstanceId): GameInstance? = instances[instanceId]

    override fun getAllInstances(): Collection<GameInstance> = instances.values

    override fun getInstancesByType(type: InstanceType): Collection<GameInstance> =
        instances.values.filter { it.type == type }

    override suspend fun heartbeat(instanceId: InstanceId, currentPlayers: Int) {
        heartbeats[instanceId] = System.currentTimeMillis()
        instances.computeIfPresent(instanceId) { _, inst -> inst.copy(currentPlayers = currentPlayers) }
    }

    fun getLastHeartbeat(instanceId: InstanceId): Long? = heartbeats[instanceId]
}

// --- DefaultRouter ---

class DefaultRouter(
    private val registry: InstanceRegistry,
    private val loadBalancer: GatewayLoadBalancer
) : Router {

    private val rules = CopyOnWriteArrayList<RouteRule>()

    override suspend fun route(request: RouteRequest): RouteResult {
        // 指定实例ID时直接路由
        request.targetInstanceId?.let { id ->
            val instance = registry.getInstance(id)
            return if (instance != null && instance.acceptingPlayers && !instance.isFull) {
                SimpleRouteResult(success = true, targetInstance = instance)
            } else {
                SimpleRouteResult(success = false, error = "目标实例不可用: ${id.value}")
            }
        }

        // 按优先级尝试路由规则
        val targetType = request.targetType
        val candidates = if (targetType != null) {
            registry.getInstancesByType(targetType).filter { it.acceptingPlayers && !it.isFull }
        } else {
            registry.getAllInstances().filter { it.acceptingPlayers && !it.isFull }
        }

        if (candidates.isEmpty()) {
            return SimpleRouteResult(success = false, error = "没有可用的游戏实例")
        }

        // 尝试匹配规则
        for (rule in rules.sortedByDescending { it.priority }) {
            if (rule.matches(request)) {
                val instance = rule.route(request, candidates)
                if (instance != null) {
                    return SimpleRouteResult(success = true, targetInstance = instance)
                }
            }
        }

        // 回退到负载均衡器
        val selected = loadBalancer.select(candidates, request.session)
        return if (selected != null) {
            SimpleRouteResult(success = true, targetInstance = selected)
        } else {
            SimpleRouteResult(success = false, error = "负载均衡器未能选择实例")
        }
    }

    override suspend fun getAvailableInstances(type: InstanceType): List<GameInstance> =
        registry.getInstancesByType(type).filter { it.acceptingPlayers && !it.isFull }

    override suspend fun getInstance(instanceId: InstanceId): GameInstance? =
        registry.getInstance(instanceId)

    override fun addRule(rule: RouteRule) {
        rules.add(rule)
        logger.info { "路由规则已添加: ${rule.name} (优先级: ${rule.priority})" }
    }

    override fun removeRule(ruleName: String) {
        rules.removeIf { it.name == ruleName }
    }
}
