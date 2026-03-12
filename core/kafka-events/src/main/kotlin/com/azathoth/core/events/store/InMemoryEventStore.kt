package com.azathoth.core.events.store

import com.azathoth.core.events.bus.Event
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 内存事件存储实现 — 用于开发/测试，生产环境应替换为数据库实现
 */
class InMemoryEventStore : EventStore {

    private data class AggregateKey(val type: String, val id: String)

    private val store = ConcurrentHashMap<AggregateKey, MutableList<SimpleStoredEvent<out Event>>>()
    private val globalStream = mutableListOf<SimpleStoredEvent<out Event>>()
    private val mutex = Mutex()

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Event> append(
        aggregateType: String,
        aggregateId: String,
        event: T,
        expectedVersion: Long?
    ): StoredEvent<T> = mutex.withLock {
        val key = AggregateKey(aggregateType, aggregateId)
        val events = store.getOrPut(key) { mutableListOf() }

        val currentVersion = events.lastOrNull()?.version ?: 0L
        if (expectedVersion != null && currentVersion != expectedVersion) {
            throw OptimisticLockException(
                "版本冲突: 期望 $expectedVersion, 实际 $currentVersion (aggregate=$aggregateType/$aggregateId)"
            )
        }

        val newVersion = currentVersion + 1
        val stored = SimpleStoredEvent(
            eventId = event.eventId.ifEmpty { UUID.randomUUID().toString() },
            eventType = event::class.qualifiedName ?: "unknown",
            aggregateId = aggregateId,
            aggregateType = aggregateType,
            version = newVersion,
            event = event,
            metadata = emptyMap(),
            createdAt = System.currentTimeMillis()
        )
        events.add(stored)
        globalStream.add(stored)
        stored as StoredEvent<T>
    }

    override suspend fun <T : Event> appendBatch(
        aggregateType: String,
        aggregateId: String,
        events: List<T>,
        expectedVersion: Long?
    ): List<StoredEvent<T>> {
        val results = mutableListOf<StoredEvent<T>>()
        var version = expectedVersion
        for (event in events) {
            val stored = append(aggregateType, aggregateId, event, version)
            results.add(stored)
            version = stored.version
        }
        return results
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Event> readEvents(
        aggregateType: String,
        aggregateId: String,
        eventClass: KClass<T>
    ): List<StoredEvent<T>> {
        val key = AggregateKey(aggregateType, aggregateId)
        return (store[key] ?: emptyList()).map { it as StoredEvent<T> }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Event> readEvents(
        aggregateType: String,
        aggregateId: String,
        fromVersion: Long,
        eventClass: KClass<T>
    ): List<StoredEvent<T>> {
        val key = AggregateKey(aggregateType, aggregateId)
        return (store[key] ?: emptyList())
            .filter { it.version >= fromVersion }
            .map { it as StoredEvent<T> }
    }

    override suspend fun getLatestVersion(aggregateType: String, aggregateId: String): Long? {
        val key = AggregateKey(aggregateType, aggregateId)
        return store[key]?.lastOrNull()?.version
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Event> readAllEvents(
        eventClass: KClass<T>,
        fromPosition: Long,
        limit: Int
    ): List<StoredEvent<T>> {
        return globalStream
            .drop(fromPosition.toInt())
            .take(limit)
            .map { it as StoredEvent<T> }
    }
}

/**
 * 乐观锁异常
 */
class OptimisticLockException(message: String) : RuntimeException(message)

/**
 * StoredEvent 的简单实现
 */
data class SimpleStoredEvent<T : Event>(
    override val eventId: String,
    override val eventType: String,
    override val aggregateId: String,
    override val aggregateType: String,
    override val version: Long,
    override val event: T,
    override val metadata: Map<String, String>,
    override val createdAt: Long
) : StoredEvent<T>

/**
 * 内存快照存储实现
 */
class InMemorySnapshotStore : SnapshotStore {

    private data class SnapshotKey(val type: String, val id: String)

    private val snapshots = ConcurrentHashMap<SnapshotKey, MutableList<SimpleEventSnapshot<out Any>>>()

    override suspend fun <T : Any> saveSnapshot(
        aggregateType: String,
        aggregateId: String,
        version: Long,
        data: T
    ) {
        val key = SnapshotKey(aggregateType, aggregateId)
        val snapshot = SimpleEventSnapshot(
            aggregateId = aggregateId,
            aggregateType = aggregateType,
            version = version,
            data = data,
            createdAt = System.currentTimeMillis()
        )
        snapshots.getOrPut(key) { mutableListOf() }.add(snapshot)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> getLatestSnapshot(
        aggregateType: String,
        aggregateId: String,
        dataClass: KClass<T>
    ): EventSnapshot<T>? {
        val key = SnapshotKey(aggregateType, aggregateId)
        return snapshots[key]?.lastOrNull() as? EventSnapshot<T>
    }

    override suspend fun deleteOldSnapshots(
        aggregateType: String,
        aggregateId: String,
        keepCount: Int
    ) {
        val key = SnapshotKey(aggregateType, aggregateId)
        val list = snapshots[key] ?: return
        if (list.size > keepCount) {
            val toRemove = list.size - keepCount
            repeat(toRemove) { list.removeFirst() }
        }
    }
}

/**
 * EventSnapshot 的简单实现
 */
data class SimpleEventSnapshot<T>(
    override val aggregateId: String,
    override val aggregateType: String,
    override val version: Long,
    override val data: T,
    override val createdAt: Long
) : EventSnapshot<T>
