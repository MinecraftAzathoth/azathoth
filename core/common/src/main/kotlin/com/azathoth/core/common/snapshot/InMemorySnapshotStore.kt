package com.azathoth.core.common.snapshot

import java.util.concurrent.ConcurrentHashMap

/**
 * 内存快照存储 — 用于开发/测试环境
 */
class InMemorySnapshotStore : SnapshotStore {

    private val records = ConcurrentHashMap<String, SnapshotRecord>()

    override suspend fun save(record: SnapshotRecord) {
        records[record.snapshotId] = record
    }

    override suspend fun saveBatch(records: List<SnapshotRecord>) {
        records.forEach { this.records[it.snapshotId] = it }
    }

    override suspend fun query(
        playerId: String,
        entityType: String?,
        limit: Int,
        beforeTimestamp: Long?
    ): List<SnapshotRecord> =
        records.values
            .filter { it.playerId == playerId }
            .let { list -> if (entityType != null) list.filter { it.entityType == entityType } else list }
            .let { list -> if (beforeTimestamp != null) list.filter { it.timestamp < beforeTimestamp } else list }
            .sortedByDescending { it.timestamp }
            .take(limit)

    override suspend fun findById(snapshotId: String): SnapshotRecord? =
        records[snapshotId]

    override suspend fun findClosest(
        playerId: String,
        entityType: String,
        timestamp: Long
    ): SnapshotRecord? =
        records.values
            .filter { it.playerId == playerId && it.entityType == entityType && it.timestamp <= timestamp }
            .maxByOrNull { it.timestamp }

    override suspend fun purge(retentionDays: Int) {
        val cutoff = System.currentTimeMillis() - retentionDays * 86_400_000L
        records.entries.removeIf { it.value.timestamp < cutoff }
    }

    /** 测试辅助：获取全部记录数 */
    fun size(): Int = records.size
}
