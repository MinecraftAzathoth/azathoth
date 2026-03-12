package com.azathoth.gradle

import kotlinx.serialization.Serializable

/**
 * 插件依赖配置
 */
@Serializable
data class PluginDependencyData(
    override val pluginId: String,
    override val versionRange: String = "*",
    override val optional: Boolean = false
) : PluginDependency

/**
 * 插件依赖配置接口
 */
interface PluginDependency {
    val pluginId: String
    val versionRange: String
    val optional: Boolean
}
