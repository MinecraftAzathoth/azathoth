package com.azathoth.website.module.market.dto

import kotlinx.serialization.Serializable

/**
 * 资源响应 DTO
 */
@Serializable
data class ResourceDTO(
    val resourceId: String,
    val name: String,
    val slug: String,
    val description: String,
    val type: String,
    val license: String,
    val authorId: String,
    val authorName: String,
    val versions: List<ResourceVersionDTO>,
    val latestVersion: String,
    val pricing: ResourcePricingDTO?,
    val downloads: Long,
    val rating: Double,
    val reviewCount: Int,
    val status: String,
    val minApiVersion: String,
    val maxApiVersion: String?,
    val dependencies: List<String>,
    val icon: String?,
    val screenshots: List<String>,
    val tags: List<String>,
    val createdAt: String,
    val updatedAt: String
)

/**
 * 资源列表项 DTO (精简版，用于列表展示)
 */
@Serializable
data class ResourceListItemDTO(
    val resourceId: String,
    val name: String,
    val slug: String,
    val description: String,
    val type: String,
    val license: String,
    val authorId: String,
    val authorName: String,
    val latestVersion: String,
    val pricing: ResourcePricingDTO?,
    val downloads: Long,
    val rating: Double,
    val reviewCount: Int,
    val icon: String?,
    val tags: List<String>
)

/**
 * 资源版本 DTO
 */
@Serializable
data class ResourceVersionDTO(
    val version: String,
    val changelog: String,
    val downloadUrl: String,
    val fileSize: Long,
    val minApiVersion: String,
    val releasedAt: String
)

/**
 * 资源定价 DTO
 */
@Serializable
data class ResourcePricingDTO(
    val price: Long,
    val currency: String,
    val subscriptionPeriod: Int? = null
)

/**
 * 资源评论 DTO
 */
@Serializable
data class ResourceReviewDTO(
    val reviewId: String,
    val resourceId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val content: String,
    val createdAt: String,
    val updatedAt: String?,
    val helpful: Int,
    val authorReply: String?
)

/**
 * 下载统计 DTO
 */
@Serializable
data class DownloadStatsDTO(
    val totalDownloads: Long,
    val lastMonthDownloads: Long,
    val lastWeekDownloads: Long,
    val versionDownloads: Map<String, Long>
)

/**
 * 分页信息 DTO
 */
@Serializable
data class PaginationDTO(
    val page: Int,
    val pageSize: Int,
    val totalCount: Long,
    val totalPages: Int
)
