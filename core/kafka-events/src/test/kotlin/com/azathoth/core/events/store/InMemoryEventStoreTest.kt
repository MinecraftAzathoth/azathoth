package com.azathoth.core.events.store

import com.azathoth.core.events.bus.Event
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryEventStoreTest {

    data class TestEvent(
        override val eventId: String = UUID.randomUUID().toString(),
        override val timestamp: Long = System.currentTimeMillis(),
        override val source: String = "test",
        val data: String = "hello"
    ) : Event

    private lateinit var store: InMemoryEventStore

    @BeforeEach
    fun setup() {
        store = InMemoryEventStore()
    }

    @Test
    fun `test append and read events`() = runTest {
        val event = TestEvent()
        val stored = store.append("Order", "order-1", event)

        assertEquals(1L, stored.version)
        assertEquals("order-1", stored.aggregateId)
        assertEquals("Order", stored.aggregateType)
        assertEquals(event.eventId, stored.event.eventId)

        val events = store.readEvents("Order", "order-1", TestEvent::class)
        assertEquals(1, events.size)
    }

    @Test
    fun `test version auto-increment`() = runTest {
        store.append("Order", "order-1", TestEvent())
        store.append("Order", "order-1", TestEvent())
        val third = store.append("Order", "order-1", TestEvent())

        assertEquals(3L, third.version)
        assertEquals(3L, store.getLatestVersion("Order", "order-1"))
    }

    @Test
    fun `test optimistic locking`() = runTest {
        store.append("Order", "order-1", TestEvent())

        // 期望版本 0，但实际已经是 1
        assertThrows(OptimisticLockException::class.java) {
            kotlinx.coroutines.test.runTest {
                store.append("Order", "order-1", TestEvent(), expectedVersion = 0)
            }
        }
    }

    @Test
    fun `test optimistic locking success`() = runTest {
        store.append("Order", "order-1", TestEvent())
        val stored = store.append("Order", "order-1", TestEvent(), expectedVersion = 1)
        assertEquals(2L, stored.version)
    }

    @Test
    fun `test readEvents from version`() = runTest {
        store.append("Order", "order-1", TestEvent(data = "a"))
        store.append("Order", "order-1", TestEvent(data = "b"))
        store.append("Order", "order-1", TestEvent(data = "c"))

        val events = store.readEvents("Order", "order-1", 2L, TestEvent::class)
        assertEquals(2, events.size)
        assertEquals(2L, events[0].version)
        assertEquals(3L, events[1].version)
    }

    @Test
    fun `test appendBatch`() = runTest {
        val events = listOf(TestEvent(data = "1"), TestEvent(data = "2"), TestEvent(data = "3"))
        val stored = store.appendBatch("Order", "order-1", events)

        assertEquals(3, stored.size)
        assertEquals(1L, stored[0].version)
        assertEquals(3L, stored[2].version)
    }

    @Test
    fun `test readAllEvents global stream`() = runTest {
        store.append("Order", "order-1", TestEvent())
        store.append("User", "user-1", TestEvent())
        store.append("Order", "order-2", TestEvent())

        val all = store.readAllEvents(TestEvent::class, 0, 100)
        assertEquals(3, all.size)
    }

    @Test
    fun `test getLatestVersion returns null for unknown aggregate`() = runTest {
        assertNull(store.getLatestVersion("Order", "unknown"))
    }
}
