package com.azathoth.sdk.api

import com.azathoth.sdk.api.command.CommandManager
import com.azathoth.sdk.api.event.EventManager
import com.azathoth.sdk.api.permission.PermissionManager

/**
 * Azathoth SDK 主入口 API
 * 
 * 提供对框架核心功能的访问
 */
interface AzathothAPI {
    /**
     * 获取玩家管理器
     */
    fun getPlayerManager(): PlayerManager
    
    /**
     * 获取世界管理器
     */
    fun getWorldManager(): WorldManager
    
    /**
     * 获取事件管理器
     */
    fun getEventManager(): EventManager

    /**
     * 获取命令管理器
     */
    fun getCommandManager(): CommandManager

    /**
     * 获取权限管理器
     */
    fun getPermissionManager(): PermissionManager
}

/**
 * 玩家管理器接口
 */
interface PlayerManager {
    // TODO: 玩家管理相关方法
}

/**
 * 世界管理器接口
 */
interface WorldManager {
    // TODO: 世界管理相关方法
}
