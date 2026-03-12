package com.azathoth.services.dungeon

import com.azathoth.services.dungeon.model.*
import com.azathoth.services.dungeon.service.DefaultDungeonRecordService
import com.azathoth.services.dungeon.service.DefaultDungeonService
import com.azathoth.services.dungeon.service.DefaultMatchmakingService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Duration

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Dungeon Service..." }

    // 业务组件
    val dungeonService = DefaultDungeonService()
    val recordService = DefaultDungeonRecordService()
    val matchmakingService = DefaultMatchmakingService(dungeonService)

    dungeonService.onInstanceCompleted = { result -> recordService.addRecord(result) }

    // 注册示例模板
    dungeonService.registerTemplate(
        SimpleDungeonTemplateInfo(
            templateId = "dungeon_forest_cave",
            name = "森林洞穴",
            description = "一个充满危险的森林洞穴",
            minPlayers = 1,
            maxPlayers = 5,
            recommendedLevel = 10,
            minLevel = 5,
            supportedDifficulties = listOf(DungeonDifficulty.NORMAL, DungeonDifficulty.HARD),
            timeLimit = Duration.ofMinutes(30),
            dailyEntryLimit = 3,
            weeklyEntryLimit = 15
        )
    )
    logger.info { "业务组件已初始化 (DungeonService, RecordService, MatchmakingService)" }

    // gRPC 服务器
    val grpcPort = System.getenv("GRPC_PORT")?.toIntOrNull() ?: 9090
    val grpcServer = ServerBuilder.forPort(grpcPort).build().start()
    logger.info { "gRPC 服务器已启动，端口: $grpcPort" }

    // HTTP 服务器
    val httpPort = System.getenv("HTTP_PORT")?.toIntOrNull() ?: 8080
    val httpServer = embeddedServer(Netty, port = httpPort) {
        routing {
            get("/health/live") { call.respondText("OK") }
            get("/health/ready") { call.respondText("OK") }
        }
    }.start(wait = false)
    logger.info { "HTTP 服务器已启动，端口: $httpPort" }

    logger.info { "Dungeon Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Dungeon Service..." }
        httpServer.stop(1000, 5000)
        grpcServer.shutdown()
        logger.info { "Dungeon Service 已关闭" }
    })

    grpcServer.awaitTermination()
}
