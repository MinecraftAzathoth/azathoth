package com.azathoth.sdk.api

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

/**
 * 事件管理器接口
 */
interface EventManager {
    // TODO: 事件管理相关方法
}
