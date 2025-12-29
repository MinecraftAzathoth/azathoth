package com.azathoth.website.module.review

/**
 * 自动检查服务接口
 */
interface AutoCheckService {
    /**
     * 执行自动检查
     */
    suspend fun runChecks(resourceId: String, fileData: ByteArray): List<ChecklistResult>

    /**
     * 检查恶意代码
     */
    suspend fun scanForMalware(fileData: ByteArray): ScanResult

    /**
     * 检查 API 兼容性
     */
    suspend fun checkApiCompatibility(fileData: ByteArray, targetApiVersion: String): CompatibilityResult

    /**
     * 检查依赖冲突
     */
    suspend fun checkDependencies(fileData: ByteArray): DependencyCheckResult
}
