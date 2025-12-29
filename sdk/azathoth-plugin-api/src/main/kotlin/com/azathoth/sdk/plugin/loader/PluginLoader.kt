package com.azathoth.sdk.plugin.loader

import com.azathoth.sdk.plugin.AzathothPlugin
import java.nio.file.Path

/**
 * 插件加载状态
 */
enum class PluginState {
    /** 已发现 */
    DISCOVERED,
    /** 加载中 */
    LOADING,
    /** 已加载 */
    LOADED,
    /** 启用中 */
    ENABLING,
    /** 已启用 */
    ENABLED,
    /** 禁用中 */
    DISABLING,
    /** 已禁用 */
    DISABLED,
    /** 卸载中 */
    UNLOADING,
    /** 已卸载 */
    UNLOADED,
    /** 错误 */
    ERROR
}

/**
 * 插件描述
 */
interface PluginDescriptor {
    /** 插件ID */
    val id: String
    
    /** 插件名称 */
    val name: String
    
    /** 版本 */
    val version: String
    
    /** 描述 */
    val description: String
    
    /** 作者列表 */
    val authors: List<String>
    
    /** 网站 */
    val website: String?
    
    /** 主类 */
    val mainClass: String
    
    /** 依赖列表 */
    val dependencies: List<PluginDependency>
    
    /** 软依赖列表 */
    val softDependencies: List<PluginDependency>
    
    /** API 版本 */
    val apiVersion: String
    
    /** 加载顺序 */
    val loadOrder: LoadOrder
}

/**
 * 插件依赖
 */
interface PluginDependency {
    /** 插件ID */
    val pluginId: String
    
    /** 最小版本 */
    val minVersion: String?
    
    /** 最大版本 */
    val maxVersion: String?
    
    /** 是否必须 */
    val required: Boolean
}

/**
 * 加载顺序
 */
enum class LoadOrder {
    /** 启动时加载 */
    STARTUP,
    /** 世界加载后 */
    POSTWORLD
}

/**
 * 已加载的插件
 */
interface LoadedPlugin {
    /** 插件描述 */
    val descriptor: PluginDescriptor
    
    /** 插件实例 */
    val instance: AzathothPlugin
    
    /** 当前状态 */
    val state: PluginState
    
    /** 插件文件路径 */
    val filePath: Path
    
    /** 数据目录 */
    val dataFolder: Path
    
    /** ClassLoader */
    val classLoader: ClassLoader
    
    /** 加载时间 */
    val loadedAt: Long
    
    /** 启用时间 */
    val enabledAt: Long?
}

/**
 * 插件加载器
 */
interface PluginLoader {
    /**
     * 发现插件
     */
    suspend fun discover(directory: Path): List<PluginDescriptor>
    
    /**
     * 加载插件
     */
    suspend fun load(descriptor: PluginDescriptor): LoadedPlugin
    
    /**
     * 启用插件
     */
    suspend fun enable(plugin: LoadedPlugin)
    
    /**
     * 禁用插件
     */
    suspend fun disable(plugin: LoadedPlugin)
    
    /**
     * 卸载插件
     */
    suspend fun unload(plugin: LoadedPlugin)
    
    /**
     * 重载插件
     */
    suspend fun reload(plugin: LoadedPlugin)
    
    /**
     * 检查依赖
     */
    fun checkDependencies(descriptor: PluginDescriptor): DependencyCheckResult
}

/**
 * 依赖检查结果
 */
interface DependencyCheckResult {
    /** 是否满足 */
    val satisfied: Boolean
    
    /** 缺失的依赖 */
    val missingDependencies: List<PluginDependency>
    
    /** 版本不兼容的依赖 */
    val incompatibleDependencies: List<PluginDependency>
}

/**
 * 插件管理器
 */
interface PluginManager {
    /**
     * 获取所有已加载插件
     */
    fun getPlugins(): Collection<LoadedPlugin>
    
    /**
     * 获取插件
     */
    fun getPlugin(id: String): LoadedPlugin?
    
    /**
     * 检查插件是否已加载
     */
    fun isPluginLoaded(id: String): Boolean
    
    /**
     * 检查插件是否已启用
     */
    fun isPluginEnabled(id: String): Boolean
    
    /**
     * 加载插件目录中的所有插件
     */
    suspend fun loadAll()
    
    /**
     * 启用所有已加载的插件
     */
    suspend fun enableAll()
    
    /**
     * 禁用所有插件
     */
    suspend fun disableAll()
    
    /**
     * 加载单个插件
     */
    suspend fun loadPlugin(path: Path): LoadedPlugin?
    
    /**
     * 启用插件
     */
    suspend fun enablePlugin(id: String)
    
    /**
     * 禁用插件
     */
    suspend fun disablePlugin(id: String)
    
    /**
     * 卸载插件
     */
    suspend fun unloadPlugin(id: String)
    
    /**
     * 重载插件
     */
    suspend fun reloadPlugin(id: String)
    
    /**
     * 添加插件状态监听器
     */
    fun addStateListener(listener: PluginStateListener)
    
    /**
     * 移除插件状态监听器
     */
    fun removeStateListener(listener: PluginStateListener)
}

/**
 * 插件状态监听器
 */
interface PluginStateListener {
    /** 状态变更 */
    suspend fun onStateChange(plugin: LoadedPlugin, oldState: PluginState, newState: PluginState)
}
