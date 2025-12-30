package com.azathoth.website.module.market.impl

import com.azathoth.website.module.market.*
import com.azathoth.website.module.market.dto.*
import com.azathoth.website.module.market.repository.MarketRepository
import com.azathoth.website.module.market.repository.ReviewRepository
import java.time.Instant
import java.util.*

/**
 * 市场服务实现
 */
class MarketServiceImpl(
    private val marketRepository: MarketRepository,
    private val reviewRepository: ReviewRepository
) : MarketService {

    override suspend fun search(criteria: MarketSearchCriteria): MarketSearchResult {
        val params = SearchParams(
            keyword = criteria.keyword,
            type = criteria.type?.name,
            license = criteria.license?.name,
            minRating = criteria.minRating,
            tags = criteria.tags,
            sortBy = criteria.sortBy.name,
            sortOrder = criteria.sortOrder.name,
            page = criteria.page,
            pageSize = criteria.pageSize
        )

        val (resources, totalCount) = marketRepository.searchResources(params)
        val totalPages = ((totalCount + criteria.pageSize - 1) / criteria.pageSize).toInt()

        return MarketSearchResultImpl(
            resources = resources.map { it.toMarketResource() },
            totalCount = totalCount,
            page = criteria.page,
            totalPages = totalPages
        )
    }

    override suspend fun getResource(resourceId: String): MarketResource? {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return null }
        return marketRepository.getResourceById(uuid)?.toMarketResource()
    }

    override suspend fun getResourceBySlug(slug: String): MarketResource? {
        return marketRepository.getResourceBySlug(slug)?.toMarketResource()
    }

    override suspend fun createResource(authorId: String, resource: CreateResourceRequest): MarketResource? {
        val uuid = try { UUID.fromString(authorId) } catch (e: Exception) { return null }

        val request = CreateResourceRequestDTO(
            name = resource.name,
            description = resource.description,
            type = resource.type.name,
            license = resource.license.name,
            pricing = resource.pricing?.toDTO(),
            minApiVersion = resource.minApiVersion,
            dependencies = resource.dependencies,
            tags = resource.tags
        )

        // 注: 实际实现中需要从认证上下文获取 authorName
        return marketRepository.createResource(uuid, "Unknown", request)?.toMarketResource()
    }

    override suspend fun updateResource(resourceId: String, update: UpdateResourceRequest): MarketResource? {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return null }

        val request = UpdateResourceRequestDTO(
            name = update.name,
            description = update.description,
            pricing = update.pricing?.toDTO(),
            icon = update.icon,
            screenshots = update.screenshots,
            tags = update.tags
        )

        return marketRepository.updateResource(uuid, request)?.toMarketResource()
    }

    override suspend fun publishVersion(resourceId: String, version: PublishVersionRequest): ResourceVersion? {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return null }

        // 注: 实际实现中需要:
        // 1. 上传文件到对象存储 (如 MinIO/S3)
        // 2. 获取下载URL和文件大小
        val downloadUrl = "https://cdn.azathoth.dev/resources/$resourceId/${version.version}/plugin.jar"
        val fileSize = version.fileData.size.toLong()

        val request = PublishVersionRequestDTO(
            version = version.version,
            changelog = version.changelog,
            minApiVersion = version.minApiVersion
        )

        return marketRepository.publishVersion(uuid, request, downloadUrl, fileSize)?.toResourceVersion()
    }

    override suspend fun deleteResource(resourceId: String): Boolean {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return false }
        return marketRepository.deleteResource(uuid)
    }

    override suspend fun getUserResources(userId: String): List<MarketResource> {
        val uuid = try { UUID.fromString(userId) } catch (e: Exception) { return emptyList() }
        return marketRepository.getUserResources(uuid).map { it.toMarketResource() }
    }

    override suspend fun getPopularResources(type: ResourceType?, limit: Int): List<MarketResource> {
        return marketRepository.getPopularResources(type?.name, limit).map { it.toMarketResource() }
    }

    override suspend fun getLatestResources(type: ResourceType?, limit: Int): List<MarketResource> {
        return marketRepository.getLatestResources(type?.name, limit).map { it.toMarketResource() }
    }

    // ========== 实现类 ==========

    private data class MarketSearchResultImpl(
        override val resources: List<MarketResource>,
        override val totalCount: Long,
        override val page: Int,
        override val totalPages: Int
    ) : MarketSearchResult

    // ========== 扩展函数 ==========

    private fun ResourceDTO.toMarketResource(): MarketResource = MarketResourceImpl(
        resourceId = this.resourceId,
        name = this.name,
        slug = this.slug,
        description = this.description,
        type = ResourceType.valueOf(this.type),
        license = LicenseType.valueOf(this.license),
        authorId = this.authorId,
        authorName = this.authorName,
        versions = this.versions.map { it.toResourceVersion() },
        latestVersion = this.latestVersion,
        pricing = this.pricing?.toResourcePricing(),
        downloads = this.downloads,
        rating = this.rating,
        reviewCount = this.reviewCount,
        status = ResourceStatus.valueOf(this.status),
        minApiVersion = this.minApiVersion,
        maxApiVersion = this.maxApiVersion,
        dependencies = this.dependencies,
        icon = this.icon ?: "",
        screenshots = this.screenshots,
        tags = this.tags,
        createdAt = Instant.parse(this.createdAt),
        updatedAt = Instant.parse(this.updatedAt)
    )

    private fun ResourceListItemDTO.toMarketResource(): MarketResource = MarketResourceImpl(
        resourceId = this.resourceId,
        name = this.name,
        slug = this.slug,
        description = this.description,
        type = ResourceType.valueOf(this.type),
        license = LicenseType.valueOf(this.license),
        authorId = this.authorId,
        authorName = this.authorName,
        versions = emptyList(),
        latestVersion = this.latestVersion,
        pricing = this.pricing?.toResourcePricing(),
        downloads = this.downloads,
        rating = this.rating,
        reviewCount = this.reviewCount,
        status = ResourceStatus.APPROVED,
        minApiVersion = "",
        maxApiVersion = null,
        dependencies = emptyList(),
        icon = this.icon ?: "",
        screenshots = emptyList(),
        tags = this.tags,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    private fun ResourceVersionDTO.toResourceVersion(): ResourceVersion = ResourceVersionImpl(
        version = this.version,
        changelog = this.changelog,
        downloadUrl = this.downloadUrl,
        fileSize = this.fileSize,
        minApiVersion = this.minApiVersion,
        releasedAt = Instant.parse(this.releasedAt)
    )

    private fun ResourcePricingDTO.toResourcePricing(): ResourcePricing = ResourcePricingImpl(
        price = this.price,
        currency = this.currency,
        subscriptionPeriod = this.subscriptionPeriod
    )

    private fun ResourcePricing.toDTO(): ResourcePricingDTO = ResourcePricingDTO(
        price = this.price,
        currency = this.currency,
        subscriptionPeriod = this.subscriptionPeriod
    )

    // ========== 数据类实现 ==========

    private data class MarketResourceImpl(
        override val resourceId: String,
        override val name: String,
        override val slug: String,
        override val description: String,
        override val type: ResourceType,
        override val license: LicenseType,
        override val authorId: String,
        override val authorName: String,
        override val versions: List<ResourceVersion>,
        override val latestVersion: String,
        override val pricing: ResourcePricing?,
        override val downloads: Long,
        override val rating: Double,
        override val reviewCount: Int,
        override val status: ResourceStatus,
        override val minApiVersion: String,
        override val maxApiVersion: String?,
        override val dependencies: List<String>,
        override val icon: String,
        override val screenshots: List<String>,
        override val tags: List<String>,
        override val createdAt: Instant,
        override val updatedAt: Instant
    ) : MarketResource

    private data class ResourceVersionImpl(
        override val version: String,
        override val changelog: String,
        override val downloadUrl: String,
        override val fileSize: Long,
        override val minApiVersion: String,
        override val releasedAt: Instant
    ) : ResourceVersion

    private data class ResourcePricingImpl(
        override val price: Long,
        override val currency: String,
        override val subscriptionPeriod: Int?
    ) : ResourcePricing
}

/**
 * 评论服务实现
 */
class ReviewServiceImpl(
    private val reviewRepository: ReviewRepository
) : ReviewService {

    override suspend fun getReviews(resourceId: String, page: Int, pageSize: Int): List<ResourceReview> {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return emptyList() }
        val (reviews, _) = reviewRepository.getReviews(uuid, page, pageSize)
        return reviews.map { it.toResourceReview() }
    }

    override suspend fun createReview(resourceId: String, userId: String, rating: Int, content: String): ResourceReview? {
        val resourceUuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return null }
        val userUuid = try { UUID.fromString(userId) } catch (e: Exception) { return null }

        // 注: 实际实现中需要从认证上下文获取 userName
        return reviewRepository.createReview(resourceUuid, userUuid, "Unknown", rating, content)?.toResourceReview()
    }

    override suspend fun updateReview(reviewId: String, rating: Int?, content: String?): ResourceReview? {
        val uuid = try { UUID.fromString(reviewId) } catch (e: Exception) { return null }
        return reviewRepository.updateReview(uuid, rating, content)?.toResourceReview()
    }

    override suspend fun deleteReview(reviewId: String): Boolean {
        val uuid = try { UUID.fromString(reviewId) } catch (e: Exception) { return false }
        return reviewRepository.deleteReview(uuid)
    }

    override suspend fun replyToReview(reviewId: String, reply: String): ResourceReview? {
        val uuid = try { UUID.fromString(reviewId) } catch (e: Exception) { return null }
        return reviewRepository.replyToReview(uuid, reply)?.toResourceReview()
    }

    override suspend fun markHelpful(reviewId: String, userId: String): Boolean {
        val reviewUuid = try { UUID.fromString(reviewId) } catch (e: Exception) { return false }
        val userUuid = try { UUID.fromString(userId) } catch (e: Exception) { return false }
        return reviewRepository.markHelpful(reviewUuid, userUuid)
    }

    // ========== 扩展函数 ==========

    private fun ResourceReviewDTO.toResourceReview(): ResourceReview = ResourceReviewImpl(
        reviewId = this.reviewId,
        resourceId = this.resourceId,
        userId = this.userId,
        userName = this.userName,
        rating = this.rating,
        content = this.content,
        createdAt = Instant.parse(this.createdAt),
        updatedAt = this.updatedAt?.let { Instant.parse(it) },
        helpful = this.helpful,
        authorReply = this.authorReply
    )

    private data class ResourceReviewImpl(
        override val reviewId: String,
        override val resourceId: String,
        override val userId: String,
        override val userName: String,
        override val rating: Int,
        override val content: String,
        override val createdAt: Instant,
        override val updatedAt: Instant?,
        override val helpful: Int,
        override val authorReply: String?
    ) : ResourceReview
}
