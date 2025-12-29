package com.azathoth.website.module.generator

/**
 * 项目生成配置
 */
interface ProjectConfig {
    val projectName: String
    val groupId: String
    val version: String
    val description: String
    val author: String
    val projectType: ProjectType
    val features: List<FeatureModule>
    val kotlinVersion: String
    val javaVersion: Int
    val includeExamples: Boolean
    val includeTests: Boolean
}

/**
 * 生成的文件
 */
interface GeneratedFile {
    val path: String
    val content: String
    val isDirectory: Boolean
}

/**
 * 生成结果
 */
interface GenerationResult {
    val success: Boolean
    val files: List<GeneratedFile>
    val downloadUrl: String?
    val error: String?
}

/**
 * 模板信息
 */
interface TemplateInfo {
    val templateId: String
    val name: String
    val description: String
    val projectType: ProjectType
    val defaultFeatures: List<FeatureModule>
    val preview: String
}

/**
 * 验证结果
 */
interface ValidationResult {
    val valid: Boolean
    val errors: List<ValidationError>
    val warnings: List<String>
}

/**
 * 验证错误
 */
interface ValidationError {
    val field: String
    val message: String
}
