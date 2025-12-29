package com.azathoth.website

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val logger = KotlinLogging.logger {}

/**
 * Azathoth Website Backend 入口点
 * 
 * 提供：
 * - REST API 服务
 * - 开发者市场后端
 * - 项目生成器服务
 */
fun main() {
    logger.info { "Starting Azathoth Website Backend" }
    
    embeddedServer(Netty, port = 8080) {
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Azathoth API Server")
        }
        
        get("/health") {
            call.respondText("OK")
        }
        
        // TODO: API 路由配置
    }
}
