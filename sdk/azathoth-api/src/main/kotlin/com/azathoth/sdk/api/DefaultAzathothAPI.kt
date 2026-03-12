package com.azathoth.sdk.api

import com.azathoth.sdk.api.command.CommandManager
import com.azathoth.sdk.api.command.DefaultCommandManager
import com.azathoth.sdk.api.event.DefaultEventManager
import com.azathoth.sdk.api.event.EventManager
import com.azathoth.sdk.api.permission.DefaultPermissionManager
import com.azathoth.sdk.api.permission.PermissionManager
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 存根玩家管理器
 */
class StubPlayerManager : PlayerManager

/**
 * 存根世界管理器
 */
class StubWorldManager : WorldManager

/**
 * AzathothAPI 默认实现
 *
 * 将 EventManager、CommandManager、PermissionManager 组装在一起，
 * PlayerManager 和 WorldManager 暂时使用存根实现。
 */
class DefaultAzathothAPI(
    val eventManager: DefaultEventManager = DefaultEventManager(),
    val commandManager: DefaultCommandManager = DefaultCommandManager(),
    val permissionManager: DefaultPermissionManager = DefaultPermissionManager(),
    private val playerManager: PlayerManager = StubPlayerManager(),
    private val worldManager: WorldManager = StubWorldManager()
) : AzathothAPI {

    init {
        logger.info { "Azathoth SDK API 初始化完成" }
    }

    override fun getPlayerManager(): PlayerManager = playerManager
    override fun getWorldManager(): WorldManager = worldManager
    override fun getEventManager(): EventManager = eventManager

    fun getCommandManager(): CommandManager = commandManager
    fun getPermissionManager(): PermissionManager = permissionManager

    companion object {
        @Volatile
        private var instance: DefaultAzathothAPI? = null

        fun getInstance(): DefaultAzathothAPI =
            instance ?: synchronized(this) {
                instance ?: DefaultAzathothAPI().also { instance = it }
            }

        fun setInstance(api: DefaultAzathothAPI) {
            instance = api
        }
    }
}
