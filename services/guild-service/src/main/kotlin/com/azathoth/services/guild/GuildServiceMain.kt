package com.azathoth.services.guild

import com.azathoth.services.guild.service.DefaultGuildService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Guild Service..." }

    val guildService = DefaultGuildService()
    logger.info { "业务组件已初始化 (GuildService)" }

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

    logger.info { "Guild Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Guild Service..." }
        httpServer.stop(1000, 5000)
        grpcServer.shutdown()
        logger.info { "Guild Service 已关闭" }
    })

    grpcServer.awaitTermination()
}
