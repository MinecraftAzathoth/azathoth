package com.azathoth.core.common.service

import java.util.concurrent.atomic.AtomicInteger

/**
 * 轮询负载均衡器
 */
class RoundRobinLoadBalancer : LoadBalancer {
    private val counter = AtomicInteger(0)

    override fun select(instances: List<ServiceInstance>): ServiceInstance? {
        if (instances.isEmpty()) return null
        val index = counter.getAndIncrement() % instances.size
        return instances[index.coerceAtLeast(0)]
    }
}

/**
 * 最少连接负载均衡器（基于权重模拟）
 */
class WeightedLoadBalancer : LoadBalancer {
    override fun select(instances: List<ServiceInstance>): ServiceInstance? {
        if (instances.isEmpty()) return null
        val totalWeight = instances.sumOf { it.weight }
        if (totalWeight <= 0) return instances.random()
        var random = (0 until totalWeight).random()
        for (instance in instances) {
            random -= instance.weight
            if (random < 0) return instance
        }
        return instances.last()
    }
}

/**
 * 随机负载均衡器
 */
class RandomLoadBalancer : LoadBalancer {
    override fun select(instances: List<ServiceInstance>): ServiceInstance? =
        instances.randomOrNull()
}
