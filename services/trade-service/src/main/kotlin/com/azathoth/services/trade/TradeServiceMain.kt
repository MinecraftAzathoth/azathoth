package com.azathoth.services.trade

import com.azathoth.services.trade.market.DefaultMarketService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Azathoth Trade Service 启动中..." }

    val marketService = DefaultMarketService()

    logger.info { "Azathoth Trade Service 已启动" }

    // 保持进程运行
    Thread.currentThread().join()
}
