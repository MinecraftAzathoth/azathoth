package com.azathoth.core.events.store

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemorySnapshotStoreTest {

    private lateinit var store: InMemorySnapshotStore

    @BeforeEach
    fun setup() {
        store = InMemorySnapshotStore()
    }

    data class OrderState(val total: Int, val items: List<String>)

    @Test
    fun `test save and get snapshot`() = runTest {
        val state = OrderState(100, listOf("item-a", "item-b"))
        store.saveSnapshot("Order", "order-1", 5L, state)

        val snapshot = store.getLatestSnapshot("Order", "order-1", OrderState::class)
        assertNotNull(snapshot)
        assertEquals(5L, snapshot!!.version)
        assertEquals(100, snapshot.data.total)
        assertEquals(2, snapshot.data.items.size)
    }

    @Test
    fun `test get latest snapshot returns most recent`() = runTest {
        store.saveSnapshot("Order", "order-1", 1L, OrderState(10, emptyList()))
        store.saveSnapshot("Order", "order-1", 5L, OrderState(50, listOf("a")))
        store.saveSnapshot("Order", "order-1", 10L, OrderState(100, listOf("a", "b")))

        val snapshot = store.getLatestSnapshot("Order", "order-1", OrderState::class)
        assertEquals(10L, snapshot!!.version)
        assertEquals(100, snapshot.data.total)
    }

    @Test
    fun `test delete old snapshots`() = runTest {
        store.saveSnapshot("Order", "order-1", 1L, OrderState(10, emptyList()))
        store.saveSnapshot("Order", "order-1", 5L, OrderState(50, listOf("a")))
        store.saveSnapshot("Order", "order-1", 10L, OrderState(100, listOf("a", "b")))

        store.deleteOldSnapshots("Order", "order-1", keepCount = 1)

        val snapshot = store.getLatestSnapshot("Order", "order-1", OrderState::class)
        assertEquals(10L, snapshot!!.version)
    }

    @Test
    fun `test get snapshot returns null for unknown aggregate`() = runTest {
        val snapshot = store.getLatestSnapshot("Order", "unknown", OrderState::class)
        assertNull(snapshot)
    }
}
