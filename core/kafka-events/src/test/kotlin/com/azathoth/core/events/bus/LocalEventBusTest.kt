package com.azathoth.core.events.bus

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class LocalEventBusTest {

    data class TestEvent(
        override val eventId: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis(),
        override val source: String = "test"
    ) : Event

    data class CancellableTestEvent(
        override val eventId: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis(),
        override val source: String = "test",
        override var cancelled: Boolean = false
    ) : CancellableEvent

    private lateinit var bus: LocalEventBus

    @BeforeEach
    fun setup() {
        bus = LocalEventBus()
    }

    @Test
    fun `test register and publish`() = runTest {
        val received = mutableListOf<TestEvent>()

        bus.register(TestEvent::class) { received.add(it) }

        val event = TestEvent()
        bus.publish(event)

        assertEquals(1, received.size)
        assertEquals(event.eventId, received[0].eventId)
    }

    @Test
    fun `test priority ordering`() = runTest {
        val order = mutableListOf<String>()

        bus.register(TestEvent::class, EventPriority.HIGH) { order.add("HIGH") }
        bus.register(TestEvent::class, EventPriority.LOW) { order.add("LOW") }
        bus.register(TestEvent::class, EventPriority.NORMAL) { order.add("NORMAL") }

        bus.publish(TestEvent())

        assertEquals(listOf("LOW", "NORMAL", "HIGH"), order)
    }

    @Test
    fun `test cancellable event`() = runTest {
        val received = mutableListOf<String>()

        bus.register(CancellableTestEvent::class, EventPriority.LOW) {
            it.cancelled = true
            received.add("canceller")
        }
        bus.register(CancellableTestEvent::class, EventPriority.NORMAL, ignoreCancelled = true) {
            received.add("ignored")
        }
        bus.register(CancellableTestEvent::class, EventPriority.HIGH, ignoreCancelled = false) {
            received.add("still-received")
        }

        val event = bus.publish(CancellableTestEvent())

        assertTrue(event.cancelled)
        assertTrue("canceller" in received)
        assertFalse("ignored" in received)
        assertTrue("still-received" in received)
    }

    @Test
    fun `test unregister via subscription`() = runTest {
        var count = 0
        val sub = bus.register(TestEvent::class) { count++ }

        bus.publish(TestEvent())
        assertEquals(1, count)

        sub.cancel()
        assertTrue(sub.isCancelled)

        bus.publish(TestEvent())
        assertEquals(1, count) // 不再接收
    }

    @Test
    fun `test unregisterAll`() = runTest {
        bus.register(TestEvent::class) {}
        bus.register(TestEvent::class) {}
        assertEquals(2, bus.getListenerCount(TestEvent::class))

        bus.unregisterAll(TestEvent::class)
        assertEquals(0, bus.getListenerCount(TestEvent::class))
    }

    @Test
    fun `test listener interface registration`() = runTest {
        val received = mutableListOf<TestEvent>()

        val listener = object : EventListener<TestEvent> {
            override val eventClass = TestEvent::class
            override val priority = EventPriority.HIGH
            override suspend fun onEvent(event: TestEvent) {
                received.add(event)
            }
        }

        bus.register(listener)
        bus.publish(TestEvent())

        assertEquals(1, received.size)
    }

    @Test
    fun `test getListenerCount`() {
        assertEquals(0, bus.getListenerCount(TestEvent::class))
        bus.register(TestEvent::class) {}
        assertEquals(1, bus.getListenerCount(TestEvent::class))
        bus.register(TestEvent::class) {}
        assertEquals(2, bus.getListenerCount(TestEvent::class))
    }
}
