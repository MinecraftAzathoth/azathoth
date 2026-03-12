package com.azathoth.services.chat

import com.azathoth.services.chat.channel.DefaultChannelManager
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "正在启动 Chat Service..." }

    val channelManager = DefaultChannelManager()

    logger.info { "Chat Service 组件初始化完成" }
    logger.info { "  - ChannelManager: DefaultChannelManager" }
    logger.info { "  - 默认频道: ${channelManager.getDefaultChannel().name}" }
}
