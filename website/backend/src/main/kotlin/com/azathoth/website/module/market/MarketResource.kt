package com.azathoth.website.module.market

import java.time.Instant

/**
 * 市场资源
 */
interface MarketResource {
    val resourceId: String
    val name: String
    val slug: String
    val description: String
    val type: ResourceType
    val license: LicenseType
    val authorId: String
    val authorName: String
    val versions: List<ResourceVersion>
    val latestVersion: String
    val pricing: ResourcePricing?
    val downloads: Long
    val rating: Double
    val reviewCount: Int
    val status: ResourceStatus
    val minApiVersion: String
    val maxApiVersion: String?
    val dependencies: List<String>
    val icon: String
    val screenshots: List<String>
    val tags: List<String>
    val createdAt: Instant
    val updatedAt: Instant
}

/**
 * 资源版本
 */
interface ResourceVersion {
    val version: String
    val changelog: String
    val downloadUrl: String
    val fileSize: Long
    val minApiVersion: String
    val releasedAt: Instant
}

/**
 * 资源定价
 */
interface ResourcePricing {
    val price: Long           // 分为单位
    val currency: String      // CNY
    val subscriptionPeriod: Int? // 订阅周期（天），null 表示买断
}

/**
 * 资源评论
 */
interface ResourceReview {
    val reviewId: String
    val resourceId: String
    val userId: String
    val userName: String
    val rating: Int           // 1-5
    val content: String
    val createdAt: Instant
    val updatedAt: Instant?
    val helpful: Int
    val authorReply: String?
}
