package com.azathoth.services.dungeon

import com.azathoth.services.dungeon.model.*
import com.azathoth.services.dungeon.service.DefaultDungeonRecordService
import com.azathoth.services.dungeon.service.DefaultDungeonService
import com.azathoth.services.dungeon.service.DefaultMatchmakingService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Dungeon Service..." }

    val dungeonService = DefaultDungeonService()
    val recordService = DefaultDungeonRecordService()
    val matchmakingService = DefaultMatchmakingService(dungeonService)

    // 副本完成时自动记录
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

    logger.info { "Dungeon Service 组件初始化完成" }
    logger.info { "  - DungeonService: DefaultDungeonService" }
    logger.info { "  - DungeonRecordService: DefaultDungeonRecordService" }
    logger.info { "  - MatchmakingService: DefaultMatchmakingService" }
}
