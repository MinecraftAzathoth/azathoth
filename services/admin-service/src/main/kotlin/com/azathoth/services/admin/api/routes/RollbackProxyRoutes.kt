package com.azathoth.services.admin.api.routes

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * 回档代理路由 — 将请求转发到 rollback-service
 *
 * admin-service 作为 BFF 层，前端只需要和 admin-service 通信。
 */
fun Route.rollbackProxyRoutes(httpClient: HttpClient) {

    val rollbackBaseUrl = System.getenv("ROLLBACK_SERVICE_URL") ?: "http://localhost:8082"

    route("/rollback") {

        // GET /api/rollback/snapshots?playerId=xxx&entityType=player
        get("/snapshots") {
            val queryString = call.request.queryString()
            val response = httpClient.get("$rollbackBaseUrl/api/snapshots?$queryString")
            call.respondBytes(response.readRawBytes(), response.contentType(), response.status)
        }

        // GET /api/rollback/snapshots/{id}
        get("/snapshots/{id}") {
            val id = call.pathParameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val response = httpClient.get("$rollbackBaseUrl/api/snapshots/$id")
            call.respondBytes(response.readRawBytes(), response.contentType(), response.status)
        }

        // POST /api/rollback/snapshot
        post("/snapshot") {
            val body = call.receiveText()
            val response = httpClient.post("$rollbackBaseUrl/api/rollback/snapshot") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            call.respondBytes(response.readRawBytes(), response.contentType(), response.status)
        }

        // POST /api/rollback/timestamp
        post("/timestamp") {
            val body = call.receiveText()
            val response = httpClient.post("$rollbackBaseUrl/api/rollback/timestamp") {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            call.respondBytes(response.readRawBytes(), response.contentType(), response.status)
        }
    }
}
