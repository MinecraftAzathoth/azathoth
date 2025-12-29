package com.azathoth.gradle.config

import org.gradle.api.provider.Property

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
