package com.azathoth.core.common.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryServiceRegistryTest {

    // 用于测试的标记接口
    interface TestService

    private lateinit var registry: InMemoryServiceRegistry

    @BeforeEach
    fun setup() {
        registry = InMemoryServiceRegistry()
    }

    @Test
    fun `test register and discover`() = runTest {
        val descriptor = SimpleServiceDescriptor.of<TestService>("test-service")
        val instance = SimpleServiceInstance("inst-1", "localhost", 8080)

        registry.register(descriptor, instance)

        val instances = registry.getInstances(TestService::class)
        assertEquals(1, instances.size)
        assertEquals("inst-1", instances[0].instanceId)
    }

    @Test
    fun `test unregister`() = runTest {
        val descriptor = SimpleServiceDescriptor.of<TestService>("test-service")
        val instance = SimpleServiceInstance("inst-1", "localhost", 8080)

        registry.register(descriptor, instance)
        registry.unregister("inst-1")

        val instances = registry.getInstances(TestService::class)
        assertTrue(instances.isEmpty())
    }

    @Test
    fun `test getHealthyInstances filters unhealthy`() = runTest {
        val descriptor = SimpleServiceDescriptor.of<TestService>("test-service")
        registry.register(descriptor, SimpleServiceInstance("inst-1", "localhost", 8080, healthy = true))
        registry.register(descriptor, SimpleServiceInstance("inst-2", "localhost", 8081, healthy = false))

        val healthy = registry.getHealthyInstances(TestService::class)
        assertEquals(1, healthy.size)
        assertEquals("inst-1", healthy[0].instanceId)
    }

    @Test
    fun `test update instance`() = runTest {
        val descriptor = SimpleServiceDescriptor.of<TestService>("test-service")
        val instance = SimpleServiceInstance("inst-1", "localhost", 8080, healthy = true)
        registry.register(descriptor, instance)

        val updated = SimpleServiceInstance("inst-1", "localhost", 8080, healthy = false)
        registry.update(updated)

        val instances = registry.getInstances(TestService::class)
        assertFalse(instances[0].healthy)
    }

    @Test
    fun `test heartbeat throws for unknown instance`() = runTest {
        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.test.runTest { registry.heartbeat("unknown") }
        }
    }

    @Test
    fun `test listener receives events`() = runTest {
        val added = mutableListOf<ServiceInstance>()
        registry.subscribe(TestService::class, object : ServiceChangeListener {
            override suspend fun onInstanceAdded(instance: ServiceInstance) { added.add(instance) }
            override suspend fun onInstanceRemoved(instance: ServiceInstance) {}
            override suspend fun onInstanceUpdated(oldInstance: ServiceInstance, newInstance: ServiceInstance) {}
        })

        val descriptor = SimpleServiceDescriptor.of<TestService>("test-service")
        registry.register(descriptor, SimpleServiceInstance("inst-1", "localhost", 8080))

        assertEquals(1, added.size)
    }
}
