package com.azathoth.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import java.io.File

/**
 * Azathoth Gradle 插件接口
 * 用于简化 Azathoth 插件开发的构建配置
 */
interface AzathothPlugin : Plugin<Project> {
    override fun apply(project: Project)
}

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
}

/**
 * 插件依赖配置
 */
interface PluginDependency {
    val pluginId: String
    val versionRange: String
    val optional: Boolean
}

/**
 * plugin.yml 生成器接口
 */
interface PluginYmlGenerator {
    /**
     * 生成 plugin.yml 文件
     */
    fun generate(extension: AzathothPluginExtension, outputDir: File)

    /**
     * 验证配置
     */
    fun validate(extension: AzathothPluginExtension): ValidationResult
}

/**
 * 验证结果
 */
interface ValidationResult {
    val valid: Boolean
    val errors: List<String>
    val warnings: List<String>
}

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
interface RelocationRule {
    val pattern: String
    val destination: String
    val excludes: List<String>
}

/**
 * 开发服务器配置
 */
interface DevServerConfig {
    /**
     * 服务器目录
     */
    val serverDir: Property<File>

    /**
     * 是否自动复制插件
     */
    val autoCopy: Property<Boolean>

    /**
     * 是否启用热重载
     */
    val hotReload: Property<Boolean>

    /**
     * JVM 参数
     */
    val jvmArgs: ListProperty<String>
}

/**
 * 发布配置
 */
interface PublishConfig {
    /**
     * 发布到 Maven 仓库
     */
    val maven: MavenPublishConfig

    /**
     * 发布到 Azathoth 市场
     */
    val marketplace: MarketplacePublishConfig
}

/**
 * Maven 发布配置
 */
interface MavenPublishConfig {
    val enabled: Property<Boolean>
    val repositoryUrl: Property<String>
    val username: Property<String>
    val password: Property<String>
}

/**
 * 市场发布配置
 */
interface MarketplacePublishConfig {
    val enabled: Property<Boolean>
    val apiKey: Property<String>
    val changelog: Property<String>
    val releaseType: Property<ReleaseType>
}

/**
 * 发布类型
 */
enum class ReleaseType {
    RELEASE,
    BETA,
    ALPHA
}
