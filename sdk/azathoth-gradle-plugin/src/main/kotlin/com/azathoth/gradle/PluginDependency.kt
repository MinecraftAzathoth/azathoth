package com.azathoth.gradle

/**
 * 插件依赖配置
 */
interface PluginDependency {
    val pluginId: String
    val versionRange: String
    val optional: Boolean
}
