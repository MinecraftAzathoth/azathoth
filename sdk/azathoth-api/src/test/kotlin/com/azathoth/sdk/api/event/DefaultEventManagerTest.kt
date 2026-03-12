package com.azathoth.sdk.api.event

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultEventManagerTest {

    private lateinit var manager: DefaultEventManager

    // 测试用事件
    data class TestEvent(val data: String) : AzathothEvent

    data class CancellableTestEvent(
        val data: String,
        override var isCancelled: Boolean = false
    ) : AzathothEvent, Cancellable

    @BeforeEach
    fun setup() {
        manager = DefaultEventManager()
    }

    @Test
    fun `functional handler receives event`() = runTest {
        var received: String? = null

        manager.registerHandler(TestEvent::class) { event ->
            received = event.data
        }

        manager.call(TestEvent("hello"))

        assertEquals("hello", received)
    }

    @Test
    fun `multiple handlers execute in priority order`() = runTest {
        val order = mutableListOf<String>()

        manager.registerHandler(TestEvent::class, priority = Priority.HIGH) {
            order.add("HIGH")
        }
        manager.registerHandler(TestEvent::class, priority = Priority.LOWEST) {
            order.add("LOWEST")
        }
        manager.registerHandler(TestEvent::class, priority = Priority.NORMAL) {
            order.add("NORMAL")
        }
        manager.registerHandler(TestEvent::class, priority = Priority.MONITOR) {
            order.add("MONITOR")
        }

        manager.call(TestEvent("test"))

        assertEquals(listOf("LOWEST", "NORMAL", "HIGH", "MONITOR"), order)
    }

    @Test
    fun `cancellable event stops propagation when ignoreCancelled is true`() = runTest {
        val order = mutableListOf<String>()

        manager.registerHandler(CancellableTestEvent::class, priority = Priority.LOW) { event ->
            order.add("LOW")
            event.isCancelled = true
        }
        manager.registerHandler(
            CancellableTestEvent::class,
            priority = Priority.NORMAL,
            ignoreCancelled = true
        ) {
            order.add("NORMAL-IGNORE")
        }
        manager.registerHandler(
            CancellableTestEvent::class,
            priority = Priority.HIGH,
            ignoreCancelled = false
        ) {
            order.add("HIGH-NO-IGNORE")
        }

        val event = manager.call(CancellableTestEvent("test"))

        assertTrue(event.isCancelled)
        assertEquals(listOf("LOW", "HIGH-NO-IGNORE"), order)
    }

    @Test
    fun `unsubscribe removes handler`() = runTest {
        var count = 0

        val subscription = manager.registerHandler(TestEvent::class) {
            count++
        }

        manager.call(TestEvent("1"))
        assertEquals(1, count)

        subscription.unsubscribe()
        assertFalse(subscription.isActive)

        manager.call(TestEvent("2"))
        assertEquals(1, count) // 不再增加
    }

    @Test
    fun `annotation-based listener registration`() = runTest {
        var received: String? = null

        val listener = object : Listener {
            @EventHandler(priority = Priority.NORMAL)
            fun onTest(event: TestEvent) {
                received = event.data
            }
        }

        manager.registerListener(listener, "testPlugin")
        manager.call(TestEvent("annotation"))

        assertEquals("annotation", received)
    }

    @Test
    fun `unregisterAll removes all handlers for a plugin`() = runTest {
        var count = 0
        val plugin = "myPlugin"

        val listener = object : Listener {
            @EventHandler
            fun onTest(event: TestEvent) {
                count++
            }
        }

        manager.registerListener(listener, plugin)
        manager.call(TestEvent("1"))
        assertEquals(1, count)

        manager.unregisterAll(plugin)
        manager.call(TestEvent("2"))
        assertEquals(1, count)
    }

    @Test
    fun `call returns the event object`() = runTest {
        manager.registerHandler(CancellableTestEvent::class) { event ->
            event.isCancelled = true
        }

        val result = manager.call(CancellableTestEvent("test"))
        assertTrue(result.isCancelled)
        assertEquals("test", result.data)
    }
}
