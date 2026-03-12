package com.azathoth.core.common.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 内存服务注册表 + 服务发现实现
 */
class InMemoryServiceRegistry : ServiceRegistry, ServiceDiscovery {

    private data class ServiceKey(val className: String, val name: String)

    private val instances = ConcurrentHashMap<String, ServiceInstance>()
    private val serviceIndex = ConcurrentHashMap<String, MutableSet<String>>() // className -> instanceIds
    private val listeners = ConcurrentHashMap<String, MutableList<ServiceChangeListener>>()
    private val mutex = Mutex()

    override suspend fun <T : Any> register(descriptor: ServiceDescriptor<T>, instance: ServiceInstance) {
        mutex.withLock {
            val className = descriptor.serviceClass.qualifiedName ?: descriptor.name
            instances[instance.instanceId] = instance
            serviceIndex.getOrPut(className) { ConcurrentHashMap.newKeySet() }.add(instance.instanceId)
            notifyAdded(className, instance)
        }
    }

    override suspend fun unregister(instanceId: String) {
        mutex.withLock {
            val instance = instances.remove(instanceId) ?: return
            serviceIndex.values.forEach { it.remove(instanceId) }
            // 通知所有监听器
            for ((className, ids) in serviceIndex) {
                if (!ids.contains(instanceId)) {
                    notifyRemoved(className, instance)
                }
            }
        }
    }

    override suspend fun update(instance: ServiceInstance) {
        mutex.withLock {
            val old = instances.put(instance.instanceId, instance)
            if (old != null) {
                for ((className, ids) in serviceIndex) {
                    if (ids.contains(instance.instanceId)) {
                        notifyUpdated(className, old, instance)
                    }
                }
            }
        }
    }

    override suspend fun heartbeat(instanceId: String) {
        // 内存实现中心跳只需确认实例存在
        instances[instanceId] ?: throw IllegalStateException("实例 $instanceId 未注册")
    }

    override suspend fun <T : Any> getInstances(serviceClass: KClass<T>): List<ServiceInstance> {
        val className = serviceClass.qualifiedName ?: return emptyList()
        val ids = serviceIndex[className] ?: return emptyList()
        return ids.mapNotNull { instances[it] }
    }

    override suspend fun <T : Any> getHealthyInstances(serviceClass: KClass<T>): List<ServiceInstance> =
        getInstances(serviceClass).filter { it.healthy }

    override suspend fun <T : Any> selectInstance(serviceClass: KClass<T>): ServiceInstance? =
        getHealthyInstances(serviceClass).randomOrNull()

    override fun <T : Any> subscribe(serviceClass: KClass<T>, listener: ServiceChangeListener) {
        val className = serviceClass.qualifiedName ?: return
        listeners.getOrPut(className) { mutableListOf() }.add(listener)
    }

    override fun <T : Any> unsubscribe(serviceClass: KClass<T>, listener: ServiceChangeListener) {
        val className = serviceClass.qualifiedName ?: return
        listeners[className]?.remove(listener)
    }

    private suspend fun notifyAdded(className: String, instance: ServiceInstance) {
        listeners[className]?.forEach { it.onInstanceAdded(instance) }
    }

    private suspend fun notifyRemoved(className: String, instance: ServiceInstance) {
        listeners[className]?.forEach { it.onInstanceRemoved(instance) }
    }

    private suspend fun notifyUpdated(className: String, old: ServiceInstance, new: ServiceInstance) {
        listeners[className]?.forEach { it.onInstanceUpdated(old, new) }
    }
}
