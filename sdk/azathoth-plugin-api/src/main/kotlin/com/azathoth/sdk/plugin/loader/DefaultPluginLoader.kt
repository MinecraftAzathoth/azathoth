package com.azathoth.sdk.plugin.loader

import com.azathoth.sdk.plugin.AzathothPlugin
import com.azathoth.sdk.plugin.annotation.Dependency
import com.azathoth.sdk.plugin.annotation.Plugin
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.jar.JarFile
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

// --- Simple data classes implementing interfaces ---

data class SimplePluginDependency(
    override val pluginId: String,
    override val minVersion: String? = null,
    override val maxVersion: String? = null,
    override val required: Boolean = true
) : PluginDependency

data class SimplePluginDescriptor(
    override val id: String,
    override val name: String,
    override val version: String,
    override val description: String = "",
    override val authors: List<String> = emptyList(),
    override val website: String? = null,
    override val mainClass: String,
    override val dependencies: List<PluginDependency> = emptyList(),
    override val softDependencies: List<PluginDependency> = emptyList(),
    override val apiVersion: String = "1.0",
    override val loadOrder: LoadOrder = LoadOrder.POSTWORLD
) : PluginDescriptor

data class SimpleLoadedPlugin(
    override val descriptor: PluginDescriptor,
    override val instance: AzathothPlugin,
    override var state: PluginState,
    override val filePath: Path,
    override val dataFolder: Path,
    override val classLoader: ClassLoader,
    override val loadedAt: Long = System.currentTimeMillis(),
    override var enabledAt: Long? = null
) : LoadedPlugin

data class SimpleDependencyCheckResult(
    override val satisfied: Boolean,
    override val missingDependencies: List<PluginDependency> = emptyList(),
    override val incompatibleDependencies: List<PluginDependency> = emptyList()
) : DependencyCheckResult

/**
 * 默认插件加载器实现
 */
class DefaultPluginLoader(
    private val loadedPlugins: () -> Map<String, LoadedPlugin> = { emptyMap() }
) : PluginLoader {

    private val classLoaders = ConcurrentHashMap<String, URLClassLoader>()
    private val descriptorCache = ConcurrentHashMap<String, Pair<PluginDescriptor, Path>>()

    override suspend fun discover(directory: Path): List<PluginDescriptor> {
        if (!directory.exists()) {
            logger.warn { "插件目录不存在: $directory" }
            return emptyList()
        }

        val descriptors = mutableListOf<PluginDescriptor>()
        directory.listDirectoryEntries("*.jar").forEach { jarPath ->
            try {
                val descriptor = readDescriptorFromJar(jarPath)
                if (descriptor != null) {
                    descriptorCache[descriptor.id] = descriptor to jarPath
                    descriptors.add(descriptor)
                    logger.info { "发现插件: ${descriptor.name} v${descriptor.version} (${jarPath.fileName})" }
                }
            } catch (e: Exception) {
                logger.error(e) { "读取插件 JAR 失败: ${jarPath.fileName}" }
            }
        }
        return descriptors
    }

    override suspend fun load(descriptor: PluginDescriptor): LoadedPlugin {
        val (_, jarPath) = descriptorCache[descriptor.id]
            ?: throw IllegalStateException("未发现插件: ${descriptor.id}")

        logger.info { "加载插件: ${descriptor.name} v${descriptor.version}" }

        val classLoader = URLClassLoader(
            arrayOf(jarPath.toUri().toURL()),
            this::class.java.classLoader
        )
        classLoaders[descriptor.id] = classLoader

        val pluginClass = classLoader.loadClass(descriptor.mainClass)
        val instance = pluginClass.getDeclaredConstructor().newInstance() as AzathothPlugin

        val dataFolder = jarPath.parent.resolve(descriptor.id)
        dataFolder.createDirectories()

        return SimpleLoadedPlugin(
            descriptor = descriptor,
            instance = instance,
            state = PluginState.LOADED,
            filePath = jarPath,
            dataFolder = dataFolder,
            classLoader = classLoader
        )
    }

    override suspend fun enable(plugin: LoadedPlugin) {
        val mutable = plugin as SimpleLoadedPlugin
        mutable.state = PluginState.ENABLING
        try {
            plugin.instance.onEnable()
            mutable.state = PluginState.ENABLED
            mutable.enabledAt = System.currentTimeMillis()
            logger.info { "插件已启用: ${plugin.descriptor.name}" }
        } catch (e: Exception) {
            mutable.state = PluginState.ERROR
            logger.error(e) { "启用插件失败: ${plugin.descriptor.name}" }
            throw e
        }
    }

    override suspend fun disable(plugin: LoadedPlugin) {
        val mutable = plugin as SimpleLoadedPlugin
        mutable.state = PluginState.DISABLING
        try {
            plugin.instance.onDisable()
            mutable.state = PluginState.DISABLED
            logger.info { "插件已禁用: ${plugin.descriptor.name}" }
        } catch (e: Exception) {
            mutable.state = PluginState.ERROR
            logger.error(e) { "禁用插件失败: ${plugin.descriptor.name}" }
            throw e
        }
    }

    override suspend fun unload(plugin: LoadedPlugin) {
        val mutable = plugin as SimpleLoadedPlugin
        mutable.state = PluginState.UNLOADING
        try {
            classLoaders.remove(plugin.descriptor.id)?.close()
            mutable.state = PluginState.UNLOADED
            logger.info { "插件已卸载: ${plugin.descriptor.name}" }
        } catch (e: Exception) {
            mutable.state = PluginState.ERROR
            logger.error(e) { "卸载插件失败: ${plugin.descriptor.name}" }
            throw e
        }
    }

    override suspend fun reload(plugin: LoadedPlugin) {
        logger.info { "重载插件: ${plugin.descriptor.name}" }
        disable(plugin)
        unload(plugin)
        val reloaded = load(plugin.descriptor)
        enable(reloaded)
    }

    override fun checkDependencies(descriptor: PluginDescriptor): DependencyCheckResult {
        val loaded = loadedPlugins()
        val missing = mutableListOf<PluginDependency>()
        val incompatible = mutableListOf<PluginDependency>()

        for (dep in descriptor.dependencies) {
            val loadedDep = loaded[dep.pluginId]
            if (loadedDep == null) {
                if (dep.required) missing.add(dep)
                continue
            }
            if (dep.minVersion != null && loadedDep.descriptor.version < dep.minVersion!!) {
                incompatible.add(dep)
            }
            if (dep.maxVersion != null && loadedDep.descriptor.version > dep.maxVersion!!) {
                incompatible.add(dep)
            }
        }

        return SimpleDependencyCheckResult(
            satisfied = missing.isEmpty() && incompatible.isEmpty(),
            missingDependencies = missing,
            incompatibleDependencies = incompatible
        )
    }

    // --- Internal helpers ---

    private fun readDescriptorFromJar(jarPath: Path): PluginDescriptor? {
        JarFile(jarPath.toFile()).use { jar ->
            // 尝试从 Manifest 读取主类，然后检查 @Plugin 注解
            val manifest = jar.manifest
            val mainClass = manifest?.mainAttributes?.getValue("Plugin-Class")
                ?: manifest?.mainAttributes?.getValue("Main-Class")

            if (mainClass != null) {
                return readDescriptorFromAnnotation(jarPath, mainClass)
            }
        }
        return null
    }

    private fun readDescriptorFromAnnotation(jarPath: Path, mainClass: String): PluginDescriptor? {
        val classLoader = URLClassLoader(
            arrayOf(jarPath.toUri().toURL()),
            this::class.java.classLoader
        )
        return try {
            val clazz = classLoader.loadClass(mainClass)
            val pluginAnnotation = clazz.getAnnotation(Plugin::class.java) ?: return null

            val dependencies = clazz.getAnnotationsByType(Dependency::class.java)
            val hardDeps = dependencies.filter { !it.soft }.map {
                SimplePluginDependency(it.id, it.minVersion.ifEmpty { null }, it.maxVersion.ifEmpty { null }, true)
            }
            val softDeps = dependencies.filter { it.soft }.map {
                SimplePluginDependency(it.id, it.minVersion.ifEmpty { null }, it.maxVersion.ifEmpty { null }, false)
            }

            SimplePluginDescriptor(
                id = pluginAnnotation.id,
                name = pluginAnnotation.name,
                version = pluginAnnotation.version,
                description = pluginAnnotation.description,
                authors = pluginAnnotation.authors.toList(),
                website = pluginAnnotation.website.ifEmpty { null },
                mainClass = mainClass,
                dependencies = hardDeps,
                softDependencies = softDeps,
                apiVersion = pluginAnnotation.apiVersion,
                loadOrder = pluginAnnotation.loadOrder
            )
        } catch (e: Exception) {
            logger.debug(e) { "无法从注解读取插件描述: $mainClass" }
            null
        } finally {
            classLoader.close()
        }
    }
}
