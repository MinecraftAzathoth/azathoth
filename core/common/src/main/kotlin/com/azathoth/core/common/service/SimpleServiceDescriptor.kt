package com.azathoth.core.common.service

import kotlin.reflect.KClass

/**
 * 简单的服务描述符实现
 */
data class SimpleServiceDescriptor<T : Any>(
    override val serviceClass: KClass<T>,
    override val name: String,
    override val version: String = "1.0.0",
    override val metadata: Map<String, String> = emptyMap()
) : ServiceDescriptor<T> {
    companion object {
        inline fun <reified T : Any> of(
            name: String = T::class.simpleName ?: "unknown",
            version: String = "1.0.0",
            metadata: Map<String, String> = emptyMap()
        ): SimpleServiceDescriptor<T> = SimpleServiceDescriptor(T::class, name, version, metadata)
    }
}

/**
 * 简单的服务实例信息实现
 */
data class SimpleServiceInstance(
    override val instanceId: String,
    override val host: String,
    override val port: Int,
    override val healthy: Boolean = true,
    override val weight: Int = 1,
    override val metadata: Map<String, String> = emptyMap()
) : ServiceInstance
