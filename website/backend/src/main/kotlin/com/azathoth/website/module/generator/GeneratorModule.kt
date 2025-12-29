package com.azathoth.website.module.generator

/**
 * 项目类型
 */
enum class ProjectType {
    GAME_PLUGIN,       // 游戏内容插件
    EXTENSION_PLUGIN,  // 扩展服务插件
    ADMIN_MODULE       // 管理后台模块
}

/**
 * 可选模块
 */
enum class FeatureModule {
    SKILL_SYSTEM,      // 技能系统
    DUNGEON_SYSTEM,    // 副本系统
    AI_BEHAVIOR,       // AI 行为
    ITEM_SYSTEM,       // 物品系统
    QUEST_SYSTEM,      // 任务系统
    COMMAND_SYSTEM,    // 命令系统
    DATABASE,          // 数据库集成
    REDIS,             // Redis 集成
    GRPC_CLIENT        // gRPC 客户端
}

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

/**
 * 模板渲染器接口
 */
interface TemplateRenderer {
    /**
     * 渲染模板
     */
    fun render(template: String, context: Map<String, Any>): String

    /**
     * 注册助手函数
     */
    fun registerHelper(name: String, helper: TemplateHelper)
}

/**
 * 模板助手函数
 */
fun interface TemplateHelper {
    fun apply(args: List<Any>, context: Map<String, Any>): String
}

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

/**
 * 打包服务接口
 */
interface PackagingService {
    /**
     * 打包为 ZIP
     */
    suspend fun packageAsZip(files: List<GeneratedFile>): ByteArray

    /**
     * 生成下载链接
     */
    suspend fun createDownloadLink(data: ByteArray, filename: String): String

    /**
     * 清理过期文件
     */
    suspend fun cleanupExpiredFiles()
}

/**
 * 代码片段库接口
 */
interface SnippetLibrary {
    /**
     * 获取技能示例
     */
    fun getSkillExample(type: String): String

    /**
     * 获取副本示例
     */
    fun getDungeonExample(type: String): String

    /**
     * 获取 AI 示例
     */
    fun getAIExample(type: String): String

    /**
     * 获取命令示例
     */
    fun getCommandExample(): String

    /**
     * 获取事件监听示例
     */
    fun getEventListenerExample(): String

    /**
     * 获取配置示例
     */
    fun getConfigExample(): String

    /**
     * 获取数据库示例
     */
    fun getDatabaseExample(): String

    /**
     * 获取 gRPC 客户端示例
     */
    fun getGrpcClientExample(): String
}
