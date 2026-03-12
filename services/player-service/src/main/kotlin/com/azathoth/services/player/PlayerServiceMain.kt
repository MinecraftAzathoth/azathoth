package com.azathoth.services.player

import com.azathoth.services.player.repository.InMemoryPlayerRepository
import com.azathoth.services.player.service.DefaultInventoryService
import com.azathoth.services.player.service.DefaultPlayerService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Player Service..." }

    val repository = InMemoryPlayerRepository()
    val playerService = DefaultPlayerService(repository)
    val inventoryService = DefaultInventoryService()

    logger.info { "Player Service 组件初始化完成" }
    logger.info { "  - PlayerRepository: InMemoryPlayerRepository" }
    logger.info { "  - PlayerService: DefaultPlayerService" }
    logger.info { "  - InventoryService: DefaultInventoryService" }
}
