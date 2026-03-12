package com.azathoth.game

import com.azathoth.core.common.AzathothConstants
import com.azathoth.game.engine.tick.DefaultTickManager
import com.azathoth.game.engine.world.DefaultWorldManager
import com.azathoth.game.mechanics.combat.DefaultCombatSystem
import com.azathoth.game.mechanics.skill.DefaultSkillSystem
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

    // 引擎子系统
    val worldManager = DefaultWorldManager()
    val tickManager = DefaultTickManager()
    logger.info { "引擎子系统已初始化 (WorldManager, TickManager)" }

    // 游戏机制
    val combatSystem = DefaultCombatSystem()
    val skillSystem = DefaultSkillSystem()
    logger.info { "游戏机制已初始化 (CombatSystem, SkillSystem)" }

    // 启动 Tick 循环
    tickManager.start()
    logger.info { "Tick 循环已启动" }

    logger.info { "Game Instance 启动完成，端口: ${AzathothConstants.DEFAULT_GATEWAY_PORT}" }

    // 关闭钩子
    Runtime.getRuntime().addShutdownHook(Thread {
        logger.info { "正在关闭 Game Instance..." }
        tickManager.stop()
        logger.info { "Game Instance 已关闭" }
    })
}
