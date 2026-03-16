package com.azathoth.core.common.snapshot

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InMemorySnapshotStoreTest {

    private lateinit var store: InMemorySnapshotStore

    @BeforeEach
    fun setup() {
        store = InMemorySnapshotStore()
    }

    @Test
    fun `save and findById`() = runTest {
        val record = makeRecord("p1", "addGold")
        store.save(record)

        val found = store.findById(record.snapshotId)
        assertNotNull(found)
        assertEquals(record.snapshotId, found.snapshotId)
        assertEquals("p1", found.playerId)
    }

    @Test
    fun `findById returns null for missing`() = runTest {
        assertNull(store.findById("nonexistent"))
    }

    @Test
    fun `query filters by playerId`() = runTest {
        store.save(makeRecord("p1", "op1"))
        store.save(makeRecord("p2", "op2"))

        val results = store.query("p1")
        assertEquals(1, results.size)
    }

    @Test
    fun `query filters by entityType`() = runTest {
        store.save(makeRecord("p1", "op1", entityType = "player"))
        store.save(makeRecord("p1", "op2", entityType = "inventory"))

        val results = store.query("p1", entityType = "inventory")
        assertEquals(1, results.size)
        assertEquals("inventory", results[0].entityType)
    }

    @Test
    fun `query respects limit`() = runTest {
        repeat(10) { store.save(makeRecord("p1", "op$it")) }

        val results = store.query("p1", limit = 3)
        assertEquals(3, results.size)
    }

    @Test
    fun `query returns descending by timestamp`() = runTest {
        store.save(makeRecord("p1", "op1", timestamp = 100))
        store.save(makeRecord("p1", "op2", timestamp = 300))
        store.save(makeRecord("p1", "op3", timestamp = 200))

        val results = store.query("p1")
        assertEquals(listOf(300L, 200L, 100L), results.map { it.timestamp })
    }

    @Test
    fun `findClosest returns nearest before timestamp`() = runTest {
        store.save(makeRecord("p1", "op1", timestamp = 1000))
        store.save(makeRecord("p1", "op2", timestamp = 2000))
        store.save(makeRecord("p1", "op3", timestamp = 3000))

        val closest = store.findClosest("p1", "player", 2500)
        assertNotNull(closest)
        assertEquals(2000L, closest.timestamp)
    }

    @Test
    fun `findClosest returns null when no match`() = runTest {
        store.save(makeRecord("p1", "op1", timestamp = 5000))

        assertNull(store.findClosest("p1", "player", 1000))
    }

    @Test
    fun `saveBatch saves all records`() = runTest {
        val records = (1..5).map { makeRecord("p1", "op$it") }
        store.saveBatch(records)
        assertEquals(5, store.size())
    }

    @Test
    fun `purge removes old records`() = runTest {
        val old = System.currentTimeMillis() - 100 * 86_400_000L
        val recent = System.currentTimeMillis()
        store.save(makeRecord("p1", "old", timestamp = old))
        store.save(makeRecord("p1", "new", timestamp = recent))

        store.purge(90)
        assertEquals(1, store.size())
    }

    private fun makeRecord(
        playerId: String,
        operation: String,
        entityType: String = "player",
        timestamp: Long = System.currentTimeMillis()
    ) = SnapshotRecord(
        snapshotId = UUID.randomUUID().toString(),
        playerId = playerId,
        entityType = entityType,
        operation = operation,
        beforeJson = """{"before":true}""",
        afterJson = """{"after":true}""",
        timestamp = timestamp
    )
}
