package com.azathoth.services.guild

import com.azathoth.services.guild.service.DefaultGuildService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Azathoth Guild Service 启动中..." }

    val guildService = DefaultGuildService()

    logger.info { "Azathoth Guild Service 已启动" }

    // 保持进程运行
    Thread.currentThread().join()
}
