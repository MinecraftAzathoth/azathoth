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
 */
class DefaultAzathothAPI(
    private val eventManager: DefaultEventManager = DefaultEventManager(),
    private val commandManager: DefaultCommandManager = DefaultCommandManager(),
    private val permissionManager: DefaultPermissionManager = DefaultPermissionManager(),
    private val playerManager: PlayerManager = StubPlayerManager(),
    private val worldManager: WorldManager = StubWorldManager()
) : AzathothAPI {

    init {
        logger.info { "Azathoth SDK API 初始化完成" }
    }

    override fun getPlayerManager(): PlayerManager = playerManager
    override fun getWorldManager(): WorldManager = worldManager
    override fun getEventManager(): EventManager = eventManager
    override fun getCommandManager(): CommandManager = commandManager
    override fun getPermissionManager(): PermissionManager = permissionManager

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
