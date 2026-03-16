package com.azathoth.services.rollback.api.model

import kotlinx.serialization.Serializable

@Serializable
data class RollbackApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null
)

@Serializable
data class SnapshotDto(
    val snapshotId: String,
    val playerId: String,
    val entityType: String,
    val operation: String,
    val beforeJson: String,
    val afterJson: String,
    val timestamp: Long,
    val sourceService: String
)

@Serializable
data class RollbackToSnapshotRequest(
    val playerId: String,
    val snapshotId: String,
    val operatorId: String
)

@Serializable
data class RollbackToTimestampRequest(
    val playerId: String,
    val entityType: String,
    val timestamp: Long,
    val operatorId: String
)

@Serializable
data class RollbackResultDto(
    val rollbackId: String,
    val playerId: String,
    val entityType: String,
    val snapshotId: String,
    val restoredJson: String,
    val timestamp: Long,
    val success: Boolean,
    val message: String? = null
)
