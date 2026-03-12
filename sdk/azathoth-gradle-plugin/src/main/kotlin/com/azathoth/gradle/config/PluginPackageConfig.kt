package com.azathoth.gradle.config

import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty

/**
 * 插件打包任务配置
 */
interface PluginPackageConfig {
    /**
     * 输出文件名
     */
    val outputFileName: Property<String>

    /**
     * 包含的文件模式
     */
    val includes: ListProperty<String>

    /**
     * 排除的文件模式
     */
    val excludes: ListProperty<String>

    /**
     * 重定位规则（用于 Shadow JAR）
     */
    val relocations: ListProperty<RelocationRule>
}

/**
 * 重定位规则
 */
data class RelocationRule(
    val pattern: String,
    val destination: String,
    val excludes: List<String> = emptyList()
)
