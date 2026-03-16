package com.azathoth.core.common.snapshot

import kotlinx.serialization.Serializable

/**
 * 快照记录 — 记录某个实体在某一时刻的完整 JSON 状态
 */
@Serializable
data class SnapshotRecord(
    /** 快照 ID（UUID） */
    val snapshotId: String,
    /** 玩家 ID */
    val playerId: String,
    /** 实体类型，如 "player" / "inventory" / "stats" */
    val entityType: String,
    /** 触发快照的操作名，如 "addGold" / "addItem" */
    val operation: String,
    /** 变更前的 JSON 快照 */
    val beforeJson: String,
    /** 变更后的 JSON 快照 */
    val afterJson: String,
    /** 快照时间戳（epoch millis） */
    val timestamp: Long,
    /** 操作来源服务 */
    val sourceService: String = "player-service"
)

/**
 * 快照存储抽象接口
 *
 * 实现可以是 ClickHouse、内存、或其他时序数据库。
 * 所有 I/O 操作都是 suspend 的，必须在协程中异步执行。
 */
interface SnapshotStore {

    /**
     * 写入一条快照记录（异步，不阻塞业务主流程）
     */
    suspend fun save(record: SnapshotRecord)

    /**
     * 批量写入快照
     */
    suspend fun saveBatch(records: List<SnapshotRecord>)

    /**
     * 查询某玩家某实体类型的快照历史（按时间倒序）
     */
    suspend fun query(
        playerId: String,
        entityType: String? = null,
        limit: Int = 50,
        beforeTimestamp: Long? = null
    ): List<SnapshotRecord>

    /**
     * 根据快照 ID 获取单条记录
     */
    suspend fun findById(snapshotId: String): SnapshotRecord?

    /**
     * 获取指定时间点之前最近的一条快照（用于回档到某时刻）
     */
    suspend fun findClosest(
        playerId: String,
        entityType: String,
        timestamp: Long
    ): SnapshotRecord?

    /**
     * 清理过期快照（保留天数）
     */
    suspend fun purge(retentionDays: Int)
}
