package com.azathoth.sdk.plugin.loader

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

private val logger = KotlinLogging.logger {}

/**
 * 默认插件管理器实现
 */
class DefaultPluginManager(
    private val pluginDirectory: Path,
    private val loader: PluginLoader = DefaultPluginLoader({ emptyMap() })
) : PluginManager {

    private val plugins = ConcurrentHashMap<String, LoadedPlugin>()
    private val listeners = CopyOnWriteArrayList<PluginStateListener>()

    // 使用能感知已加载插件的 loader
    private val pluginLoader: PluginLoader = if (loader is DefaultPluginLoader) {
        DefaultPluginLoader { plugins.toMap() }
    } else {
        loader
    }

    override fun getPlugins(): Collection<LoadedPlugin> = plugins.values

    override fun getPlugin(id: String): LoadedPlugin? = plugins[id]

    override fun isPluginLoaded(id: String): Boolean =
        plugins[id]?.let { it.state == PluginState.LOADED || it.state == PluginState.ENABLED } ?: false

    override fun isPluginEnabled(id: String): Boolean =
        plugins[id]?.state == PluginState.ENABLED

    override suspend fun loadAll() {
        logger.info { "从目录加载所有插件: $pluginDirectory" }
        val descriptors = pluginLoader.discover(pluginDirectory)
        val sorted = resolveDependencyOrder(descriptors)

        for (descriptor in sorted) {
            try {
                val depCheck = pluginLoader.checkDependencies(descriptor)
                if (!depCheck.satisfied) {
                    logger.warn { "插件 ${descriptor.name} 依赖不满足，跳过加载" }
                    continue
                }
                val loaded = pluginLoader.load(descriptor)
                plugins[descriptor.id] = loaded
                notifyStateChange(loaded, PluginState.DISCOVERED, PluginState.LOADED)
            } catch (e: Exception) {
                logger.error(e) { "加载插件失败: ${descriptor.name}" }
            }
        }
    }

    override suspend fun enableAll() {
        logger.info { "启用所有已加载插件" }
        for (plugin in plugins.values) {
            if (plugin.state == PluginState.LOADED || plugin.state == PluginState.DISABLED) {
                try {
                    enablePlugin(plugin.descriptor.id)
                } catch (e: Exception) {
                    logger.error(e) { "启用插件失败: ${plugin.descriptor.name}" }
                }
            }
        }
    }

    override suspend fun disableAll() {
        logger.info { "禁用所有插件" }
        // 反序禁用
        for (plugin in plugins.values.reversed()) {
            if (plugin.state == PluginState.ENABLED) {
                try {
                    disablePlugin(plugin.descriptor.id)
                } catch (e: Exception) {
                    logger.error(e) { "禁用插件失败: ${plugin.descriptor.name}" }
                }
            }
        }
    }

    override suspend fun loadPlugin(path: Path): LoadedPlugin? {
        val descriptors = pluginLoader.discover(path.parent)
        val descriptor = descriptors.find {
            path.fileName.toString().contains(it.id) || descriptors.size == 1
        } ?: descriptors.firstOrNull()

        if (descriptor == null) {
            logger.warn { "未在 $path 中发现插件" }
            return null
        }

        val loaded = pluginLoader.load(descriptor)
        plugins[descriptor.id] = loaded
        notifyStateChange(loaded, PluginState.DISCOVERED, PluginState.LOADED)
        return loaded
    }

    override suspend fun enablePlugin(id: String) {
        val plugin = plugins[id] ?: throw IllegalArgumentException("插件未找到: $id")
        val oldState = plugin.state
        pluginLoader.enable(plugin)
        notifyStateChange(plugin, oldState, PluginState.ENABLED)
    }

    override suspend fun disablePlugin(id: String) {
        val plugin = plugins[id] ?: throw IllegalArgumentException("插件未找到: $id")
        val oldState = plugin.state
        pluginLoader.disable(plugin)
        notifyStateChange(plugin, oldState, PluginState.DISABLED)
    }

    override suspend fun unloadPlugin(id: String) {
        val plugin = plugins[id] ?: throw IllegalArgumentException("插件未找到: $id")
        if (plugin.state == PluginState.ENABLED) {
            disablePlugin(id)
        }
        val oldState = plugin.state
        pluginLoader.unload(plugin)
        plugins.remove(id)
        notifyStateChange(plugin, oldState, PluginState.UNLOADED)
    }

    override suspend fun reloadPlugin(id: String) {
        val plugin = plugins[id] ?: throw IllegalArgumentException("插件未找到: $id")
        val descriptor = plugin.descriptor
        unloadPlugin(id)
        val reloaded = pluginLoader.load(descriptor)
        plugins[descriptor.id] = reloaded
        pluginLoader.enable(reloaded)
        notifyStateChange(reloaded, PluginState.UNLOADED, PluginState.ENABLED)
    }

    override fun addStateListener(listener: PluginStateListener) {
        listeners.add(listener)
    }

    override fun removeStateListener(listener: PluginStateListener) {
        listeners.remove(listener)
    }

    // --- Internal ---

    private suspend fun notifyStateChange(plugin: LoadedPlugin, oldState: PluginState, newState: PluginState) {
        for (listener in listeners) {
            try {
                listener.onStateChange(plugin, oldState, newState)
            } catch (e: Exception) {
                logger.error(e) { "状态监听器异常" }
            }
        }
    }

    /**
     * 拓扑排序：按依赖顺序排列插件
     */
    private fun resolveDependencyOrder(descriptors: List<PluginDescriptor>): List<PluginDescriptor> {
        val byId = descriptors.associateBy { it.id }
        val visited = mutableSetOf<String>()
        val result = mutableListOf<PluginDescriptor>()

        fun visit(descriptor: PluginDescriptor) {
            if (descriptor.id in visited) return
            visited.add(descriptor.id)
            for (dep in descriptor.dependencies) {
                byId[dep.pluginId]?.let { visit(it) }
            }
            result.add(descriptor)
        }

        descriptors.forEach { visit(it) }
        return result
    }
}
