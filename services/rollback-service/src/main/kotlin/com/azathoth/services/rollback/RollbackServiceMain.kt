package com.azathoth.services.rollback

import com.azathoth.core.common.snapshot.InMemorySnapshotStore
import com.azathoth.services.rollback.api.routes.rollbackRoutes
import com.azathoth.services.rollback.service.DefaultRollbackService
import com.azathoth.services.rollback.store.ClickHouseSnapshotStore
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Rollback Service..." }

    // ClickHouse 数据源
    val useClickHouse = System.getenv("CLICKHOUSE_ENABLED")?.toBoolean() ?: false
    val snapshotStore = if (useClickHouse) {
        val ds = HikariDataSource(HikariConfig().apply {
            jdbcUrl = System.getenv("CLICKHOUSE_URL") ?: "jdbc:clickhouse://localhost:8123/azathoth"
            username = System.getenv("CLICKHOUSE_USER") ?: "azathoth"
            password = System.getenv("CLICKHOUSE_PASSWORD") ?: "azathoth_dev"
            driverClassName = "com.clickhouse.jdbc.ClickHouseDriver"
            maximumPoolSize = 5
        })
        val store = ClickHouseSnapshotStore(ds)
        runBlocking { store.initSchema() }
        logger.info { "使用 ClickHouse 快照存储" }
        store
    } else {
        logger.info { "使用内存快照存储（设置 CLICKHOUSE_ENABLED=true 启用 ClickHouse）" }
        InMemorySnapshotStore()
    }

    val rollbackService = DefaultRollbackService(snapshotStore)
    logger.info { "业务组件已初始化 (RollbackService)" }

    // HTTP 服务器
    val httpPort = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 8082
    val httpServer = embeddedServer(Netty, port = httpPort) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }

        install(CORS) {
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.Authorization)
            allowHeader(HttpHeaders.ContentType)
            allowCredentials = true
            anyHost()
        }

        install(StatusPages) {
            exception<Throwable> { call, cause ->
                logger.error(cause) { "未处理的异常: ${cause.message}" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("success" to false, "error" to (cause.message ?: "内部服务器错误"))
                )
            }
        }

        routing {
            get("/health/live") { call.respondText("OK") }
            get("/health/ready") { call.respondText("OK") }

            route("/api") {
                rollbackRoutes(rollbackService)
            }
        }
    }.start(wait = false)
    logger.info { "HTTP 服务器已启动，端口: $httpPort" }

    logger.info { "Rollback Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Rollback Service..." }
        httpServer.stop(1000, 5000)
        logger.info { "Rollback Service 已关闭" }
    })

    Thread.currentThread().join()
}
