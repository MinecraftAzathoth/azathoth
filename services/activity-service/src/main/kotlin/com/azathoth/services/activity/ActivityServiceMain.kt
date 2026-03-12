package com.azathoth.services.activity

import com.azathoth.services.activity.model.*
import com.azathoth.services.activity.service.DefaultAchievementService
import com.azathoth.services.activity.service.DefaultActivityScheduler
import com.azathoth.services.activity.service.DefaultActivityService
import com.azathoth.services.activity.service.DefaultQuestService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.grpc.ServerBuilder
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Activity Service..." }

    // 业务组件
    val activityService = DefaultActivityService()
    val questService = DefaultQuestService()
    val achievementService = DefaultAchievementService()
    val scheduler = DefaultActivityScheduler(activityService)

    // 注册示例活动
    activityService.registerActivity(
        SimpleActivityInfo(
            activityId = "summer_event",
            name = "夏日祭典",
            description = "限时夏日活动",
            type = ActivityType.SEASONAL,
            state = ActivityState.ACTIVE,
            startTime = Instant.now(),
            endTime = Instant.now().plus(Duration.ofDays(30)),
            config = SimpleActivityConfig(minLevel = 1),
            rewards = emptyList()
        )
    )

    // 注册示例任务
    questService.registerQuest(
        SimpleQuestInfo(
            questId = "daily_kill_monsters",
            name = "每日讨伐",
            description = "击杀10只怪物",
            type = QuestType.DAILY,
            objectives = listOf(
                SimpleQuestObjective("kill_monsters", "击杀怪物", "kill", 10)
            ),
            rewards = listOf(
                SimpleQuestReward("gold", 100, experience = 50)
            )
        )
    )

    // 注册示例成就
    achievementService.registerAchievement(
        SimpleAchievementInfo(
            achievementId = "monster_slayer",
            name = "怪物猎人",
            description = "击杀大量怪物",
            category = AchievementCategory.COMBAT,
            points = 10,
            icon = "sword",
            tiers = listOf(
                SimpleAchievementTier(1, "初级猎人", 100, listOf(SimpleQuestReward("gold", 50))),
                SimpleAchievementTier(2, "高级猎人", 500, listOf(SimpleQuestReward("gold", 200))),
                SimpleAchievementTier(3, "传奇猎人", 1000, listOf(SimpleQuestReward("gold", 500)))
            )
        )
    )
    logger.info { "业务组件已初始化 (ActivityService, QuestService, AchievementService, Scheduler)" }

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

    logger.info { "Activity Service 启动完成" }

    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Activity Service..." }
        httpServer.stop(1000, 5000)
        grpcServer.shutdown()
        logger.info { "Activity Service 已关闭" }
    })

    grpcServer.awaitTermination()
}
