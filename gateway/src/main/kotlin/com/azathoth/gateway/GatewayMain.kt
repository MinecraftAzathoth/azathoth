package com.azathoth.gateway

import com.azathoth.core.common.AzathothConstants
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Azathoth Gateway 入口点
 * 
 * Gateway 负责：
 * - 玩家连接管理和认证
 * - 负载均衡和路由
 * - 与 Game Instance 的无缝传输
 */
suspend fun main(args: Array<String>) {
    logger.info { "Starting ${AzathothConstants.NAME} Gateway v${AzathothConstants.VERSION}" }
    
    // TODO: 初始化 Gateway 服务器
    // TODO: 启动 gRPC 客户端连接到后端服务
    // TODO: 启动 HTTP API
    
    logger.info { "Gateway started on port ${AzathothConstants.DEFAULT_GATEWAY_PORT}" }
}
