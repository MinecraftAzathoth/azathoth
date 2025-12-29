package com.azathoth.gradle

import java.io.File

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
