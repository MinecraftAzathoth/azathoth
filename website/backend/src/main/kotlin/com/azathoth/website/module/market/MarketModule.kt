package com.azathoth.website.module.market

import java.time.Instant

/**
 * 资源类型
 */
enum class ResourceType {
    PLUGIN,      // 游戏插件
    MODULE,      // 功能模块
    SERVICE,     // 独立服务
    TEMPLATE,    // 项目模板
    THEME,       // 后台主题
    TOOL         // 开发工具
}

/**
 * 授权类型
 */
enum class LicenseType {
    FREE_OPEN_SOURCE,   // 免费开源
    FREE_CLOSED_SOURCE, // 免费闭源
    PAID_PERPETUAL,     // 付费买断
    PAID_SUBSCRIPTION   // 付费订阅
}

/**
 * 审核状态
 */
enum class ResourceStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

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

/**
 * 搜索条件
 */
interface MarketSearchCriteria {
    val keyword: String?
    val type: ResourceType?
    val license: LicenseType?
    val minRating: Double?
    val tags: List<String>?
    val sortBy: MarketSortBy
    val sortOrder: SortOrder
    val page: Int
    val pageSize: Int
}

/**
 * 排序方式
 */
enum class MarketSortBy {
    RELEVANCE,
    DOWNLOADS,
    RATING,
    UPDATED,
    CREATED
}

/**
 * 排序顺序
 */
enum class SortOrder {
    ASC, DESC
}

/**
 * 搜索结果
 */
interface MarketSearchResult {
    val resources: List<MarketResource>
    val totalCount: Long
    val page: Int
    val totalPages: Int
}

/**
 * 市场服务接口
 */
interface MarketService {
    /**
     * 搜索资源
     */
    suspend fun search(criteria: MarketSearchCriteria): MarketSearchResult

    /**
     * 获取资源详情
     */
    suspend fun getResource(resourceId: String): MarketResource?

    /**
     * 获取资源（通过 slug）
     */
    suspend fun getResourceBySlug(slug: String): MarketResource?

    /**
     * 创建资源
     */
    suspend fun createResource(authorId: String, resource: CreateResourceRequest): MarketResource?

    /**
     * 更新资源
     */
    suspend fun updateResource(resourceId: String, update: UpdateResourceRequest): MarketResource?

    /**
     * 发布新版本
     */
    suspend fun publishVersion(resourceId: String, version: PublishVersionRequest): ResourceVersion?

    /**
     * 删除资源
     */
    suspend fun deleteResource(resourceId: String): Boolean

    /**
     * 获取用户的资源列表
     */
    suspend fun getUserResources(userId: String): List<MarketResource>

    /**
     * 获取热门资源
     */
    suspend fun getPopularResources(type: ResourceType? = null, limit: Int = 10): List<MarketResource>

    /**
     * 获取最新资源
     */
    suspend fun getLatestResources(type: ResourceType? = null, limit: Int = 10): List<MarketResource>
}

/**
 * 创建资源请求
 */
interface CreateResourceRequest {
    val name: String
    val description: String
    val type: ResourceType
    val license: LicenseType
    val pricing: ResourcePricing?
    val minApiVersion: String
    val dependencies: List<String>
    val tags: List<String>
}

/**
 * 更新资源请求
 */
interface UpdateResourceRequest {
    val name: String?
    val description: String?
    val pricing: ResourcePricing?
    val icon: String?
    val screenshots: List<String>?
    val tags: List<String>?
}

/**
 * 发布版本请求
 */
interface PublishVersionRequest {
    val version: String
    val changelog: String
    val fileData: ByteArray
    val minApiVersion: String
}

/**
 * 评论服务接口
 */
interface ReviewService {
    /**
     * 获取资源评论
     */
    suspend fun getReviews(resourceId: String, page: Int, pageSize: Int): List<ResourceReview>

    /**
     * 创建评论
     */
    suspend fun createReview(resourceId: String, userId: String, rating: Int, content: String): ResourceReview?

    /**
     * 更新评论
     */
    suspend fun updateReview(reviewId: String, rating: Int?, content: String?): ResourceReview?

    /**
     * 删除评论
     */
    suspend fun deleteReview(reviewId: String): Boolean

    /**
     * 作者回复
     */
    suspend fun replyToReview(reviewId: String, reply: String): ResourceReview?

    /**
     * 标记有帮助
     */
    suspend fun markHelpful(reviewId: String, userId: String): Boolean
}

/**
 * 下载服务接口
 */
interface DownloadService {
    /**
     * 获取下载链接
     */
    suspend fun getDownloadUrl(resourceId: String, version: String, userId: String?): String?

    /**
     * 记录下载
     */
    suspend fun recordDownload(resourceId: String, version: String, userId: String?): Boolean

    /**
     * 获取下载统计
     */
    suspend fun getDownloadStats(resourceId: String): DownloadStats
}

/**
 * 下载统计
 */
interface DownloadStats {
    val totalDownloads: Long
    val lastMonthDownloads: Long
    val lastWeekDownloads: Long
    val versionDownloads: Map<String, Long>
}
