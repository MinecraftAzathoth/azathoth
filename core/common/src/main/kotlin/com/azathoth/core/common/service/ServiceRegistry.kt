package com.azathoth.core.common.service

import kotlin.reflect.KClass

/**
 * 服务描述符
 */
interface ServiceDescriptor<T : Any> {
    /** 服务类型 */
    val serviceClass: KClass<T>
    
    /** 服务名称 */
    val name: String
    
    /** 服务版本 */
    val version: String
    
    /** 服务元数据 */
    val metadata: Map<String, String>
}

/**
 * 服务实例信息
 */
interface ServiceInstance {
    /** 实例ID */
    val instanceId: String
    
    /** 主机地址 */
    val host: String
    
    /** 端口 */
    val port: Int
    
    /** 是否健康 */
    val healthy: Boolean
    
    /** 权重 */
    val weight: Int
    
    /** 元数据 */
    val metadata: Map<String, String>
}

/**
 * 服务注册表
 */
interface ServiceRegistry {
    /** 注册服务 */
    suspend fun <T : Any> register(descriptor: ServiceDescriptor<T>, instance: ServiceInstance)
    
    /** 注销服务 */
    suspend fun unregister(instanceId: String)
    
    /** 更新服务实例 */
    suspend fun update(instance: ServiceInstance)
    
    /** 发送心跳 */
    suspend fun heartbeat(instanceId: String)
}

/**
 * 服务发现
 */
interface ServiceDiscovery {
    /** 获取服务的所有实例 */
    suspend fun <T : Any> getInstances(serviceClass: KClass<T>): List<ServiceInstance>
    
    /** 获取服务的健康实例 */
    suspend fun <T : Any> getHealthyInstances(serviceClass: KClass<T>): List<ServiceInstance>
    
    /** 选择一个服务实例 */
    suspend fun <T : Any> selectInstance(serviceClass: KClass<T>): ServiceInstance?
    
    /** 订阅服务变更 */
    fun <T : Any> subscribe(serviceClass: KClass<T>, listener: ServiceChangeListener)
    
    /** 取消订阅 */
    fun <T : Any> unsubscribe(serviceClass: KClass<T>, listener: ServiceChangeListener)
}

/**
 * 服务变更监听器
 */
interface ServiceChangeListener {
    /** 服务实例添加 */
    suspend fun onInstanceAdded(instance: ServiceInstance)
    
    /** 服务实例移除 */
    suspend fun onInstanceRemoved(instance: ServiceInstance)
    
    /** 服务实例更新 */
    suspend fun onInstanceUpdated(oldInstance: ServiceInstance, newInstance: ServiceInstance)
}

/**
 * 负载均衡器
 */
interface LoadBalancer {
    /** 从实例列表中选择一个 */
    fun select(instances: List<ServiceInstance>): ServiceInstance?
}
