package com.azathoth.services.admin

import com.azathoth.services.admin.moderation.DefaultModerationService
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Azathoth Admin Service 启动中..." }

    val moderationService = DefaultModerationService()

    logger.info { "Azathoth Admin Service 已启动" }

    // 保持进程运行
    Thread.currentThread().join()
}
