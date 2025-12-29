package com.azathoth.game

import com.azathoth.core.common.AzathothConstants
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Azathoth Game Instance 入口点
 * 
 * Game Instance 负责：
 * - 基于 Minestom 的游戏服务器运行时
 * - 游戏逻辑处理
 * - 与 Gateway 的协调
 */
suspend fun main(args: Array<String>) {
    logger.info { "Starting ${AzathothConstants.NAME} Game Instance v${AzathothConstants.VERSION}" }
    
    // TODO: 初始化 Minestom 服务器
    // TODO: 加载插件和游戏机制
    // TODO: 连接到 Gateway 进行注册
    
    logger.info { "Game Instance initialized" }
}
