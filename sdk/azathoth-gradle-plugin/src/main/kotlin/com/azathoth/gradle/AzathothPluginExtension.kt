package com.azathoth.gradle

import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import com.azathoth.gradle.config.DevServerConfig
import com.azathoth.gradle.config.PluginPackageConfig
import com.azathoth.gradle.config.PublishConfig
import javax.inject.Inject

/**
 * Azathoth 插件扩展配置
 */
interface AzathothPluginExtension {
    /**
     * 插件唯一标识
     */
    val id: Property<String>

    /**
     * 插件版本
     */
    val version: Property<String>

    /**
     * 插件名称
     */
    val name: Property<String>

    /**
     * 插件描述
     */
    val description: Property<String>

    /**
     * 插件作者
     */
    val author: Property<String>

    /**
     * 主类全限定名
     */
    val mainClass: Property<String>

    /**
     * API 版本兼容性
     */
    val apiVersion: Property<String>

    /**
     * 是否自动生成 plugin.yml
     */
    val generatePluginYml: Property<Boolean>

    /**
     * 是否打包依赖（Shadow JAR）
     */
    val shadowDependencies: Property<Boolean>

    /**
     * 依赖声明
     */
    val dependencies: ListProperty<PluginDependency>

    /**
     * 可选依赖
     */
    val softDependencies: ListProperty<String>

    /**
     * 提供的服务
     */
    val providedServices: ListProperty<String>

    /**
     * 消费的服务
     */
    val consumedServices: ListProperty<String>

    /**
     * 开发服务器配置
     */
    val devServer: DevServerConfig

    /**
     * 打包配置
     */
    val packaging: PluginPackageConfig

    /**
     * 添加插件依赖的便捷方法
     */
    fun dependency(pluginId: String, versionRange: String = "*", optional: Boolean = false) {
        dependencies.add(PluginDependencyData(pluginId, versionRange, optional))
    }

    /**
     * 配置开发服务器
     */
    fun devServer(action: Action<DevServerConfig>) {
        action.execute(devServer)
    }

    /**
     * 配置打包
     */
    fun packaging(action: Action<PluginPackageConfig>) {
        action.execute(packaging)
    }
}
