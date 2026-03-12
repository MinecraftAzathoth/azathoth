package com.azathoth.services.admin

import com.azathoth.services.admin.moderation.DefaultModerationService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Admin Service..." }

    val moderationService = DefaultModerationService()
    logger.info { "业务组件已初始化 (ModerationService)" }

    val grpcPort = System.getenv("GRPC_PORT")?.toIntOrNull() ?: 9090
    val grpcServer = ServerBuilder.forPort(grpcPort).build().start()
    logger.info { "gRPC 服务器已启动，端口: $grpcPort" }

    val httpPort = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 8080
    val httpServer = embeddedServer(Netty, port = httpPort) {
        routing {
            get("/health/live") { call.respondText("OK") }
            get("/health/ready") { call.respondText("OK") }
        }
    }.start(wait = false)
    logger.info { "HTTP 服务器已启动，端口: $httpPort" }

    logger.info { "Admin Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Admin Service..." }
        httpServer.stop(1000, 5000)
        grpcServer.shutdown()
        logger.info { "Admin Service 已关闭" }
    })

    grpcServer.awaitTermination()
}
