package com.azathoth.website.module.market.dto

import kotlinx.serialization.Serializable

/**
 * 通用 API 响应
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDTO? = null
)

/**
 * 错误信息
 */
@Serializable
data class ErrorDTO(
    val code: String,
    val message: String
)

/**
 * 资源搜索响应
 */
@Serializable
data class SearchResourcesResponse(
    val success: Boolean,
    val resources: List<ResourceListItemDTO>,
    val pagination: PaginationDTO
)

/**
 * 单个资源响应
 */
@Serializable
data class ResourceResponse(
    val success: Boolean,
    val resource: ResourceDTO? = null,
    val error: ErrorDTO? = null
)

/**
 * 资源列表响应
 */
@Serializable
data class ResourceListResponse(
    val success: Boolean,
    val resources: List<ResourceListItemDTO>
)

/**
 * 评论列表响应
 */
@Serializable
data class ReviewListResponse(
    val success: Boolean,
    val reviews: List<ResourceReviewDTO>,
    val pagination: PaginationDTO
)

/**
 * 单个评论响应
 */
@Serializable
data class ReviewResponse(
    val success: Boolean,
    val review: ResourceReviewDTO? = null,
    val error: ErrorDTO? = null
)

/**
 * 版本响应
 */
@Serializable
data class VersionResponse(
    val success: Boolean,
    val version: ResourceVersionDTO? = null,
    val error: ErrorDTO? = null
)

/**
 * 下载链接响应
 */
@Serializable
data class DownloadUrlResponse(
    val success: Boolean,
    val downloadUrl: String? = null,
    val expiresAt: String? = null,
    val error: ErrorDTO? = null
)

/**
 * 下载统计响应
 */
@Serializable
data class DownloadStatsResponse(
    val success: Boolean,
    val stats: DownloadStatsDTO? = null
)

/**
 * 简单成功响应
 */
@Serializable
data class SimpleResponse(
    val success: Boolean,
    val error: ErrorDTO? = null
)
