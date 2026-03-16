package com.azathoth.services.rollback.service

import com.azathoth.core.common.snapshot.SnapshotRecord
import com.azathoth.core.common.snapshot.SnapshotStore
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.Serializable
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * 回档结果
 */
@Serializable
data class RollbackResult(
    val rollbackId: String,
    val playerId: String,
    val entityType: String,
    val snapshotId: String,
    val restoredJson: String,
    val timestamp: Long,
    val success: Boolean,
    val message: String? = null
)

/**
 * 回档服务接口
 */
interface RollbackService {

    /**
     * 查询玩家快照历史
     */
    suspend fun querySnapshots(
        playerId: String,
        entityType: String? = null,
        limit: Int = 50,
        beforeTimestamp: Long? = null
    ): List<SnapshotRecord>

    /**
     * 根据快照 ID 获取快照详情
     */
    suspend fun getSnapshot(snapshotId: String): SnapshotRecord?

    /**
     * 回档到指定快照（返回该快照的 beforeJson 作为恢复数据）
     */
    suspend fun rollbackToSnapshot(
        playerId: String,
        snapshotId: String,
        operatorId: String
    ): RollbackResult

    /**
     * 回档到指定时间点（找到最近的快照）
     */
    suspend fun rollbackToTimestamp(
        playerId: String,
        entityType: String,
        timestamp: Long,
        operatorId: String
    ): RollbackResult

    /**
     * 清理过期快照
     */
    suspend fun purgeOldSnapshots(retentionDays: Int)
}

/**
 * 默认回档服务实现
 */
class DefaultRollbackService(
    private val snapshotStore: SnapshotStore
) : RollbackService {

    override suspend fun querySnapshots(
        playerId: String,
        entityType: String?,
        limit: Int,
        beforeTimestamp: Long?
    ): List<SnapshotRecord> =
        snapshotStore.query(playerId, entityType, limit, beforeTimestamp)

    override suspend fun getSnapshot(snapshotId: String): SnapshotRecord? =
        snapshotStore.findById(snapshotId)

    override suspend fun rollbackToSnapshot(
        playerId: String,
        snapshotId: String,
        operatorId: String
    ): RollbackResult {
        val snapshot = snapshotStore.findById(snapshotId)
            ?: return RollbackResult(
                rollbackId = UUID.randomUUID().toString(),
                playerId = playerId,
                entityType = "unknown",
                snapshotId = snapshotId,
                restoredJson = "{}",
                timestamp = System.currentTimeMillis(),
                success = false,
                message = "快照不存在: $snapshotId"
            )

        if (snapshot.playerId != playerId) {
            return RollbackResult(
                rollbackId = UUID.randomUUID().toString(),
                playerId = playerId,
                entityType = snapshot.entityType,
                snapshotId = snapshotId,
                restoredJson = "{}",
                timestamp = System.currentTimeMillis(),
                success = false,
                message = "快照不属于该玩家"
            )
        }

        logger.info { "回档玩家 $playerId 到快照 $snapshotId (操作员: $operatorId)" }

        // 记录回档操作本身作为一条新快照
        val rollbackRecord = SnapshotRecord(
            snapshotId = UUID.randomUUID().toString(),
            playerId = playerId,
            entityType = snapshot.entityType,
            operation = "rollback",
            beforeJson = snapshot.afterJson,
            afterJson = snapshot.beforeJson,
            timestamp = System.currentTimeMillis(),
            sourceService = "rollback-service"
        )
        snapshotStore.save(rollbackRecord)

        return RollbackResult(
            rollbackId = rollbackRecord.snapshotId,
            playerId = playerId,
            entityType = snapshot.entityType,
            snapshotId = snapshotId,
            restoredJson = snapshot.beforeJson,
            timestamp = System.currentTimeMillis(),
            success = true,
            message = "已回档到快照 ${snapshot.operation}@${snapshot.timestamp}"
        )
    }

    override suspend fun rollbackToTimestamp(
        playerId: String,
        entityType: String,
        timestamp: Long,
        operatorId: String
    ): RollbackResult {
        val snapshot = snapshotStore.findClosest(playerId, entityType, timestamp)
            ?: return RollbackResult(
                rollbackId = UUID.randomUUID().toString(),
                playerId = playerId,
                entityType = entityType,
                snapshotId = "",
                restoredJson = "{}",
                timestamp = System.currentTimeMillis(),
                success = false,
                message = "未找到该时间点之前的快照"
            )

        return rollbackToSnapshot(playerId, snapshot.snapshotId, operatorId)
    }

    override suspend fun purgeOldSnapshots(retentionDays: Int) {
        snapshotStore.purge(retentionDays)
        logger.info { "已清理 $retentionDays 天前的快照数据" }
    }
}
