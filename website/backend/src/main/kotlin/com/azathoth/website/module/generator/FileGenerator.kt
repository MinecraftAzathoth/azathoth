package com.azathoth.website.module.generator

/**
 * 文件生成器接口
 */
interface FileGenerator {
    /**
     * 生成 build.gradle.kts
     */
    fun generateBuildGradle(config: ProjectConfig): String

    /**
     * 生成 settings.gradle.kts
     */
    fun generateSettingsGradle(config: ProjectConfig): String

    /**
     * 生成 libs.versions.toml
     */
    fun generateVersionCatalog(config: ProjectConfig): String

    /**
     * 生成 plugin.yml
     */
    fun generatePluginYml(config: ProjectConfig): String

    /**
     * 生成主类
     */
    fun generateMainClass(config: ProjectConfig): String

    /**
     * 生成示例代码
     */
    fun generateExamples(config: ProjectConfig): List<GeneratedFile>

    /**
     * 生成测试代码
     */
    fun generateTests(config: ProjectConfig): List<GeneratedFile>

    /**
     * 生成 .gitignore
     */
    fun generateGitignore(): String

    /**
     * 生成 README.md
     */
    fun generateReadme(config: ProjectConfig): String
}
