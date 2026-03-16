package com.azathoth.services.rollback

import com.azathoth.core.common.snapshot.InMemorySnapshotStore
import com.azathoth.core.common.snapshot.SnapshotRecord
import com.azathoth.services.rollback.service.DefaultRollbackService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultRollbackServiceTest {

    private lateinit var store: InMemorySnapshotStore
    private lateinit var service: DefaultRollbackService

    @BeforeEach
    fun setup() {
        store = InMemorySnapshotStore()
        service = DefaultRollbackService(store)
    }

    @Test
    fun `querySnapshots returns records for player`() = runTest {
        val playerId = "player-1"
        repeat(3) { i ->
            store.save(makeRecord(playerId, "addGold", timestamp = 1000L + i))
        }
        store.save(makeRecord("player-2", "addGold", timestamp = 2000L))

        val results = service.querySnapshots(playerId)
        assertEquals(3, results.size)
        assertTrue(results.all { it.playerId == playerId })
    }

    @Test
    fun `querySnapshots filters by entityType`() = runTest {
        val playerId = "player-1"
        store.save(makeRecord(playerId, "addGold", entityType = "player"))
        store.save(makeRecord(playerId, "addItem", entityType = "inventory"))

        val playerOnly = service.querySnapshots(playerId, entityType = "player")
        assertEquals(1, playerOnly.size)
        assertEquals("player", playerOnly[0].entityType)
    }

    @Test
    fun `getSnapshot returns correct record`() = runTest {
        val record = makeRecord("player-1", "addGold")
        store.save(record)

        val found = service.getSnapshot(record.snapshotId)
        assertNotNull(found)
        assertEquals(record.snapshotId, found.snapshotId)
    }

    @Test
    fun `getSnapshot returns null for missing id`() = runTest {
        val found = service.getSnapshot("nonexistent")
        assertNull(found)
    }

    @Test
    fun `rollbackToSnapshot succeeds with valid snapshot`() = runTest {
        val playerId = "player-1"
        val record = makeRecord(playerId, "addGold", beforeJson = """{"gold":100}""", afterJson = """{"gold":200}""")
        store.save(record)

        val result = service.rollbackToSnapshot(playerId, record.snapshotId, "admin-1")
        assertTrue(result.success)
        assertEquals("""{"gold":100}""", result.restoredJson)
        assertEquals(playerId, result.playerId)
    }

    @Test
    fun `rollbackToSnapshot fails for nonexistent snapshot`() = runTest {
        val result = service.rollbackToSnapshot("player-1", "nonexistent", "admin-1")
        assertFalse(result.success)
        assertTrue(result.message?.contains("快照不存在") == true)
    }

    @Test
    fun `rollbackToSnapshot fails when player mismatch`() = runTest {
        val record = makeRecord("player-1", "addGold")
        store.save(record)

        val result = service.rollbackToSnapshot("player-2", record.snapshotId, "admin-1")
        assertFalse(result.success)
        assertTrue(result.message?.contains("不属于该玩家") == true)
    }

    @Test
    fun `rollbackToSnapshot creates a rollback snapshot record`() = runTest {
        val playerId = "player-1"
        val record = makeRecord(playerId, "addGold")
        store.save(record)

        val initialSize = store.size()
        service.rollbackToSnapshot(playerId, record.snapshotId, "admin-1")
        assertEquals(initialSize + 1, store.size())
    }

    @Test
    fun `rollbackToTimestamp finds closest snapshot`() = runTest {
        val playerId = "player-1"
        store.save(makeRecord(playerId, "op1", timestamp = 1000L, beforeJson = """{"v":1}"""))
        store.save(makeRecord(playerId, "op2", timestamp = 2000L, beforeJson = """{"v":2}"""))
        store.save(makeRecord(playerId, "op3", timestamp = 3000L, beforeJson = """{"v":3}"""))

        val result = service.rollbackToTimestamp(playerId, "player", 2500L, "admin-1")
        assertTrue(result.success)
        assertEquals("""{"v":2}""", result.restoredJson)
    }

    @Test
    fun `rollbackToTimestamp fails when no snapshot before timestamp`() = runTest {
        val playerId = "player-1"
        store.save(makeRecord(playerId, "op1", timestamp = 5000L))

        val result = service.rollbackToTimestamp(playerId, "player", 1000L, "admin-1")
        assertFalse(result.success)
    }

    @Test
    fun `purgeOldSnapshots removes old records`() = runTest {
        val old = System.currentTimeMillis() - 100 * 86_400_000L // 100 天前
        val recent = System.currentTimeMillis()
        store.save(makeRecord("p1", "op", timestamp = old))
        store.save(makeRecord("p2", "op", timestamp = recent))

        service.purgeOldSnapshots(90)
        assertEquals(1, store.size())
    }

    private fun makeRecord(
        playerId: String,
        operation: String,
        entityType: String = "player",
        timestamp: Long = System.currentTimeMillis(),
        beforeJson: String = """{"before":true}""",
        afterJson: String = """{"after":true}"""
    ) = SnapshotRecord(
        snapshotId = UUID.randomUUID().toString(),
        playerId = playerId,
        entityType = entityType,
        operation = operation,
        beforeJson = beforeJson,
        afterJson = afterJson,
        timestamp = timestamp
    )
}
