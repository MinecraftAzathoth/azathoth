package com.azathoth.website.module.review

/**
 * 扫描结果
 */
interface ScanResult {
    val clean: Boolean
    val threats: List<Threat>
}

/**
 * 威胁
 */
interface Threat {
    val type: String
    val severity: ThreatSeverity
    val description: String
    val location: String?
}

/**
 * 兼容性结果
 */
interface CompatibilityResult {
    val compatible: Boolean
    val issues: List<CompatibilityIssue>
}

/**
 * 兼容性问题
 */
interface CompatibilityIssue {
    val type: String
    val description: String
    val suggestion: String?
}

/**
 * 依赖检查结果
 */
interface DependencyCheckResult {
    val valid: Boolean
    val missingDependencies: List<String>
    val conflictingDependencies: List<DependencyConflict>
}

/**
 * 依赖冲突
 */
interface DependencyConflict {
    val dependency: String
    val requiredVersion: String
    val conflictingVersion: String
}
