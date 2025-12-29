package com.azathoth.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Azathoth Gradle 插件接口
 * 用于简化 Azathoth 插件开发的构建配置
 */
interface AzathothPlugin : Plugin<Project> {
    override fun apply(project: Project)
}
