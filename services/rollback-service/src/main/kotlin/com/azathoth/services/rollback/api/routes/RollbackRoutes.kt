package com.azathoth.services.rollback.api.routes

import com.azathoth.services.rollback.api.model.*
import com.azathoth.services.rollback.service.RollbackService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * 回档 API 路由
 */
fun Route.rollbackRoutes(rollbackService: RollbackService) {

    route("/snapshots") {

        // GET /snapshots?playerId=xxx&entityType=player&limit=50&before=timestamp
        get {
            val playerId = call.queryParameters["playerId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    RollbackApiResponse<Unit>(success = false, error = "缺少 playerId 参数")
                )
            val entityType = call.queryParameters["entityType"]
            val limit = call.queryParameters["limit"]?.toIntOrNull() ?: 50
            val before = call.queryParameters["before"]?.toLongOrNull()

            val records = rollbackService.querySnapshots(playerId, entityType, limit, before)
            val dtos = records.map {
                SnapshotDto(
                    snapshotId = it.snapshotId,
                    playerId = it.playerId,
                    entityType = it.entityType,
                    operation = it.operation,
                    beforeJson = it.beforeJson,
                    afterJson = it.afterJson,
                    timestamp = it.timestamp,
                    sourceService = it.sourceService
                )
            }
            call.respond(RollbackApiResponse(success = true, data = dtos))
        }

        // GET /snapshots/{id}
        get("/{id}") {
            val id = call.pathParameters["id"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)
            val record = rollbackService.getSnapshot(id)
            if (record == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    RollbackApiResponse<Unit>(success = false, error = "快照不存在")
                )
            } else {
                call.respond(
                    RollbackApiResponse(
                        success = true,
                        data = SnapshotDto(
                            snapshotId = record.snapshotId,
                            playerId = record.playerId,
                            entityType = record.entityType,
                            operation = record.operation,
                            beforeJson = record.beforeJson,
                            afterJson = record.afterJson,
                            timestamp = record.timestamp,
                            sourceService = record.sourceService
                        )
                    )
                )
            }
        }
    }

    route("/rollback") {

        // POST /rollback/snapshot — 回档到指定快照
        post("/snapshot") {
            val req = call.receive<RollbackToSnapshotRequest>()
            val result = rollbackService.rollbackToSnapshot(req.playerId, req.snapshotId, req.operatorId)
            val dto = RollbackResultDto(
                rollbackId = result.rollbackId,
                playerId = result.playerId,
                entityType = result.entityType,
                snapshotId = result.snapshotId,
                restoredJson = result.restoredJson,
                timestamp = result.timestamp,
                success = result.success,
                message = result.message
            )
            val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
            call.respond(status, RollbackApiResponse(success = result.success, data = dto, error = if (!result.success) result.message else null))
        }

        // POST /rollback/timestamp — 回档到指定时间点
        post("/timestamp") {
            val req = call.receive<RollbackToTimestampRequest>()
            val result = rollbackService.rollbackToTimestamp(req.playerId, req.entityType, req.timestamp, req.operatorId)
            val dto = RollbackResultDto(
                rollbackId = result.rollbackId,
                playerId = result.playerId,
                entityType = result.entityType,
                snapshotId = result.snapshotId,
                restoredJson = result.restoredJson,
                timestamp = result.timestamp,
                success = result.success,
                message = result.message
            )
            val status = if (result.success) HttpStatusCode.OK else HttpStatusCode.BadRequest
            call.respond(status, RollbackApiResponse(success = result.success, data = dto, error = if (!result.success) result.message else null))
        }
    }
}
