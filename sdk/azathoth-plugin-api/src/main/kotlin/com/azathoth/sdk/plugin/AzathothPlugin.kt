package com.azathoth.sdk.plugin

/**
 * Azathoth 插件基础接口
 * 
 * 所有插件必须实现此接口
 */
interface AzathothPlugin {
    /** 插件唯一标识符 */
    val id: String
    
    /** 插件名称 */
    val name: String
    
    /** 插件版本 */
    val version: String
    
    /** 插件描述 */
    val description: String get() = ""
    
    /** 插件作者 */
    val authors: List<String> get() = emptyList()
    
    /**
     * 插件启用时调用
     */
    suspend fun onEnable()
    
    /**
     * 插件禁用时调用
     */
    suspend fun onDisable()
}
