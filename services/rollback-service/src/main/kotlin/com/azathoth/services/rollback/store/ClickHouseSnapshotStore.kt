package com.azathoth.services.rollback.store

import com.azathoth.core.common.snapshot.SnapshotRecord
import com.azathoth.core.common.snapshot.SnapshotStore
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

/**
 * ClickHouse 快照存储实现
 *
 * 使用 MergeTree 引擎，按 (player_id, entity_type, timestamp) 排序，
 * 适合高吞吐写入和按时间范围查询。
 */
class ClickHouseSnapshotStore(private val dataSource: DataSource) : SnapshotStore {

    /** 建表（幂等） */
    suspend fun initSchema() = withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.createStatement().execute(
                """
                CREATE TABLE IF NOT EXISTS player_snapshots (
                    snapshot_id   String,
                    player_id     String,
                    entity_type   LowCardinality(String),
                    operation     LowCardinality(String),
                    before_json   String,
                    after_json    String,
                    timestamp     Int64,
                    source_service LowCardinality(String),
                    event_date    Date DEFAULT toDate(toDateTime(timestamp / 1000))
                ) ENGINE = MergeTree()
                PARTITION BY toYYYYMM(event_date)
                ORDER BY (player_id, entity_type, timestamp)
                TTL event_date + INTERVAL 90 DAY
                """.trimIndent()
            )
        }
        logger.info { "ClickHouse player_snapshots 表已就绪" }
    }

    override suspend fun save(record: SnapshotRecord) = withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(INSERT_SQL).use { ps ->
                bindRecord(ps, record)
                ps.executeUpdate()
            }
        }
        Unit
    }

    override suspend fun saveBatch(records: List<SnapshotRecord>) = withContext(Dispatchers.IO) {
        if (records.isEmpty()) return@withContext
        dataSource.connection.use { conn ->
            conn.prepareStatement(INSERT_SQL).use { ps ->
                for (r in records) {
                    bindRecord(ps, r)
                    ps.addBatch()
                }
                ps.executeBatch()
            }
        }
        Unit
    }

    override suspend fun query(
        playerId: String,
        entityType: String?,
        limit: Int,
        beforeTimestamp: Long?
    ): List<SnapshotRecord> = withContext(Dispatchers.IO) {
        val sb = StringBuilder("SELECT * FROM player_snapshots WHERE player_id = ?")
        if (entityType != null) sb.append(" AND entity_type = ?")
        if (beforeTimestamp != null) sb.append(" AND timestamp < ?")
        sb.append(" ORDER BY timestamp DESC LIMIT ?")

        dataSource.connection.use { conn ->
            conn.prepareStatement(sb.toString()).use { ps ->
                var idx = 1
                ps.setString(idx++, playerId)
                if (entityType != null) ps.setString(idx++, entityType)
                if (beforeTimestamp != null) ps.setLong(idx++, beforeTimestamp)
                ps.setInt(idx, limit)
                val rs = ps.executeQuery()
                buildList {
                    while (rs.next()) add(rs.toRecord())
                }
            }
        }
    }

    override suspend fun findById(snapshotId: String): SnapshotRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM player_snapshots WHERE snapshot_id = ? LIMIT 1").use { ps ->
                ps.setString(1, snapshotId)
                val rs = ps.executeQuery()
                if (rs.next()) rs.toRecord() else null
            }
        }
    }

    override suspend fun findClosest(
        playerId: String,
        entityType: String,
        timestamp: Long
    ): SnapshotRecord? = withContext(Dispatchers.IO) {
        dataSource.connection.use { conn ->
            conn.prepareStatement(
                """
                SELECT * FROM player_snapshots
                WHERE player_id = ? AND entity_type = ? AND timestamp <= ?
                ORDER BY timestamp DESC LIMIT 1
                """.trimIndent()
            ).use { ps ->
                ps.setString(1, playerId)
                ps.setString(2, entityType)
                ps.setLong(3, timestamp)
                val rs = ps.executeQuery()
                if (rs.next()) rs.toRecord() else null
            }
        }
    }

    override suspend fun purge(retentionDays: Int) = withContext(Dispatchers.IO) {
        val cutoff = System.currentTimeMillis() - retentionDays * 86_400_000L
        dataSource.connection.use { conn ->
            conn.prepareStatement("ALTER TABLE player_snapshots DELETE WHERE timestamp < ?").use { ps ->
                ps.setLong(1, cutoff)
                ps.executeUpdate()
            }
        }
        logger.info { "已清理 $retentionDays 天前的快照" }
    }

    companion object {
        private const val INSERT_SQL = """
            INSERT INTO player_snapshots
            (snapshot_id, player_id, entity_type, operation, before_json, after_json, timestamp, source_service)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """

        private fun bindRecord(ps: java.sql.PreparedStatement, r: SnapshotRecord) {
            ps.setString(1, r.snapshotId)
            ps.setString(2, r.playerId)
            ps.setString(3, r.entityType)
            ps.setString(4, r.operation)
            ps.setString(5, r.beforeJson)
            ps.setString(6, r.afterJson)
            ps.setLong(7, r.timestamp)
            ps.setString(8, r.sourceService)
        }

        private fun java.sql.ResultSet.toRecord() = SnapshotRecord(
            snapshotId = getString("snapshot_id"),
            playerId = getString("player_id"),
            entityType = getString("entity_type"),
            operation = getString("operation"),
            beforeJson = getString("before_json"),
            afterJson = getString("after_json"),
            timestamp = getLong("timestamp"),
            sourceService = getString("source_service")
        )
    }
}
