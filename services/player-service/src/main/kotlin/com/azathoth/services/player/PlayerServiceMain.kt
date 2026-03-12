package com.azathoth.services.player

import com.azathoth.services.player.grpc.PlayerServiceGrpcImpl
import com.azathoth.services.player.repository.InMemoryPlayerRepository
import com.azathoth.services.player.service.DefaultInventoryService
import com.azathoth.services.player.service.DefaultPlayerService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Player Service..." }

    // 业务组件
    val repository = InMemoryPlayerRepository()
    val playerService = DefaultPlayerService(repository)
    val inventoryService = DefaultInventoryService()
    logger.info { "业务组件已初始化 (PlayerService, InventoryService)" }

    // gRPC 服务器
    val grpcPort = System.getenv("GRPC_PORT")?.toIntOrNull() ?: 9090
    val grpcServer = ServerBuilder.forPort(grpcPort)
        .addService(PlayerServiceGrpcImpl(playerService))
        .build()
        .start()
    logger.info { "gRPC 服务器已启动，端口: $grpcPort (已注册: PlayerService)" }

    // HTTP 服务器（健康检查 + API）
    val httpPort = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 8080
    val httpServer = embeddedServer(Netty, port = httpPort) {
        routing {
            get("/health/live") { call.respondText("OK") }
            get("/health/ready") { call.respondText("OK") }
        }
    }.start(wait = false)
    logger.info { "HTTP 服务器已启动，端口: $httpPort" }

    logger.info { "Player Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Player Service..." }
        httpServer.stop(1000, 5000)
        grpcServer.shutdown()
        logger.info { "Player Service 已关闭" }
    })

    grpcServer.awaitTermination()
}
