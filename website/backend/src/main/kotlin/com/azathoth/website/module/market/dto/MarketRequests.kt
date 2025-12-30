package com.azathoth.website.module.market.dto

import kotlinx.serialization.Serializable

/**
 * 创建资源请求
 */
@Serializable
data class CreateResourceRequestDTO(
    val name: String,
    val description: String,
    val type: String,
    val license: String,
    val pricing: ResourcePricingDTO? = null,
    val minApiVersion: String,
    val dependencies: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

/**
 * 更新资源请求
 */
@Serializable
data class UpdateResourceRequestDTO(
    val name: String? = null,
    val description: String? = null,
    val pricing: ResourcePricingDTO? = null,
    val icon: String? = null,
    val screenshots: List<String>? = null,
    val tags: List<String>? = null
)

/**
 * 发布版本请求
 */
@Serializable
data class PublishVersionRequestDTO(
    val version: String,
    val changelog: String,
    val minApiVersion: String
)

/**
 * 创建评论请求
 */
@Serializable
data class CreateReviewRequestDTO(
    val rating: Int,
    val content: String
)

/**
 * 回复评论请求
 */
@Serializable
data class ReplyReviewRequestDTO(
    val reply: String
)

/**
 * 搜索参数
 */
data class SearchParams(
    val keyword: String? = null,
    val type: String? = null,
    val license: String? = null,
    val minRating: Double? = null,
    val tags: List<String>? = null,
    val sortBy: String = "DOWNLOADS",
    val sortOrder: String = "DESC",
    val page: Int = 1,
    val pageSize: Int = 20
)
