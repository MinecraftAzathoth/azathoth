package com.azathoth.website.module.generator

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
