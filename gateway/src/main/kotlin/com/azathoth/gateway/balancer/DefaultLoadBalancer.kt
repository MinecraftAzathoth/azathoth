package com.azathoth.gateway.balancer

import com.azathoth.gateway.routing.GameInstance
import com.azathoth.gateway.session.PlayerSession
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

// --- DefaultGatewayLoadBalancer ---

class DefaultGatewayLoadBalancer(
    override val strategy: BalancingStrategy = BalancingStrategy.ROUND_ROBIN
) : GatewayLoadBalancer {

    private val roundRobinCounter = AtomicInteger(0)
    private val connectionCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val random = java.util.Random()

    override fun select(instances: List<GameInstance>, session: PlayerSession?): GameInstance? {
        if (instances.isEmpty()) return null
        return when (strategy) {
            BalancingStrategy.ROUND_ROBIN -> selectRoundRobin(instances)
            BalancingStrategy.LEAST_CONNECTIONS -> selectLeastConnections(instances)
            BalancingStrategy.RANDOM -> instances[random.nextInt(instances.size)]
            BalancingStrategy.WEIGHTED_ROUND_ROBIN -> selectWeightedRoundRobin(instances)
            else -> selectRoundRobin(instances)
        }
    }

    override fun selectWeighted(instances: List<InstanceWeight>, session: PlayerSession?): GameInstance? {
        if (instances.isEmpty()) return null
        val totalWeight = instances.sumOf { it.effectiveWeight }
        if (totalWeight <= 0) return instances.first().instance

        var target = random.nextInt(totalWeight)
        for (iw in instances) {
            target -= iw.effectiveWeight
            if (target < 0) return iw.instance
        }
        return instances.last().instance
    }

    override fun updateStats(instance: GameInstance, responseTimeMs: Long, success: Boolean) {
        val key = instance.instanceId.value
        if (success) {
            connectionCounts.getOrPut(key) { AtomicInteger(0) }.incrementAndGet()
        }
    }

    override fun resetStats() {
        connectionCounts.clear()
        roundRobinCounter.set(0)
    }

    private fun selectRoundRobin(instances: List<GameInstance>): GameInstance {
        val idx = roundRobinCounter.getAndIncrement() % instances.size
        return instances[Math.floorMod(idx, instances.size)]
    }

    private fun selectLeastConnections(instances: List<GameInstance>): GameInstance =
        instances.minByOrNull { it.currentPlayers } ?: instances.first()

    private fun selectWeightedRoundRobin(instances: List<GameInstance>): GameInstance {
        // 按可用容量加权
        val weighted = instances.map { it to it.availableCapacity.coerceAtLeast(1) }
        val totalWeight = weighted.sumOf { it.second }
        var target = random.nextInt(totalWeight.coerceAtLeast(1))
        for ((inst, weight) in weighted) {
            target -= weight
            if (target < 0) return inst
        }
        return instances.last()
    }
}

// --- SimpleHealthStatus ---

data class SimpleHealthStatus(
    override val instance: GameInstance,
    override val healthy: Boolean,
    override val responseTimeMs: Long,
    override val checkTime: Long = System.currentTimeMillis(),
    override val error: String? = null,
    override val consecutiveFailures: Int = 0
) : HealthStatus

// --- DefaultHealthChecker ---

class DefaultHealthChecker(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    private val instancesProvider: () -> List<GameInstance> = { emptyList() }
) : HealthChecker {

    private val listeners = ConcurrentHashMap.newKeySet<HealthCheckListener>()
    private val statusMap = ConcurrentHashMap<String, SimpleHealthStatus>()
    private var periodicJob: Job? = null

    override suspend fun check(instance: GameInstance): HealthStatus {
        val start = System.currentTimeMillis()
        // 简单的健康检查：实例是否接受玩家且未满
        val healthy = instance.acceptingPlayers && !instance.isFull
        val elapsed = System.currentTimeMillis() - start
        val key = instance.instanceId.value
        val old = statusMap[key]
        val failures = if (healthy) 0 else (old?.consecutiveFailures ?: 0) + 1

        val status = SimpleHealthStatus(
            instance = instance,
            healthy = healthy,
            responseTimeMs = elapsed,
            consecutiveFailures = failures
        )
        statusMap[key] = status

        // 通知监听器
        if (old?.healthy != status.healthy) {
            listeners.forEach { listener ->
                listener.onHealthChange(instance, old, status)
                if (!status.healthy) listener.onUnhealthy(instance, status)
                if (status.healthy && old?.healthy == false) listener.onRecovered(instance, status)
            }
        }
        return status
    }

    override suspend fun checkAll(instances: List<GameInstance>): Map<GameInstance, HealthStatus> =
        instances.associateWith { check(it) }

    override fun startPeriodicCheck(intervalMs: Long) {
        periodicJob?.cancel()
        periodicJob = scope.launch {
            while (isActive) {
                try {
                    val instances = instancesProvider()
                    checkAll(instances)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error(e) { "健康检查异常" }
                }
                delay(intervalMs)
            }
        }
        logger.info { "定期健康检查已启动, 间隔: ${intervalMs}ms" }
    }

    override fun stopPeriodicCheck() {
        periodicJob?.cancel()
        periodicJob = null
        logger.info { "定期健康检查已停止" }
    }

    override fun addListener(listener: HealthCheckListener) {
        listeners.add(listener)
    }
}
