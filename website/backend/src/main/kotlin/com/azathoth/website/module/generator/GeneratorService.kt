package com.azathoth.website.module.generator

/**
 * 项目生成器服务接口
 */
interface GeneratorService {
    /**
     * 获取可用模板
     */
    suspend fun getTemplates(): List<TemplateInfo>

    /**
     * 获取支持的 Kotlin 版本
     */
    suspend fun getSupportedKotlinVersions(): List<String>

    /**
     * 获取支持的 Java 版本
     */
    suspend fun getSupportedJavaVersions(): List<Int>

    /**
     * 获取最新 API 版本
     */
    suspend fun getLatestApiVersion(): String

    /**
     * 验证项目配置
     */
    suspend fun validateConfig(config: ProjectConfig): ValidationResult

    /**
     * 生成项目
     */
    suspend fun generate(config: ProjectConfig): GenerationResult

    /**
     * 预览生成的文件列表
     */
    suspend fun preview(config: ProjectConfig): List<String>
}
