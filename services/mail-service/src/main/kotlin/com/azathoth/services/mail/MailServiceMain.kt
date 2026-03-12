package com.azathoth.services.mail

import com.azathoth.services.mail.grpc.MailServiceGrpcImpl
import com.azathoth.services.mail.service.DefaultMailService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Mail Service..." }

    val mailService = DefaultMailService()
    logger.info { "业务组件已初始化 (MailService)" }

    // gRPC 服务器
    val grpcPort = System.getenv("GRPC_PORT")?.toIntOrNull() ?: 9090
    val grpcServer = ServerBuilder.forPort(grpcPort)
        .addService(MailServiceGrpcImpl(mailService))
        .build()
        .start()
    logger.info { "gRPC 服务器已启动，端口: $grpcPort (已注册: MailService)" }

    // HTTP 服务器（健康检查）
    val httpPort = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 8080
    val httpServer = embeddedServer(Netty, port = httpPort) {
        routing {
            get("/health/live") { call.respondText("OK") }
            get("/health/ready") { call.respondText("OK") }
        }
    }.start(wait = false)
    logger.info { "HTTP 服务器已启动，端口: $httpPort" }

    logger.info { "Mail Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Mail Service..." }
        httpServer.stop(1000, 5000)
        grpcServer.shutdown()
        logger.info { "Mail Service 已关闭" }
    })

    grpcServer.awaitTermination()
}
