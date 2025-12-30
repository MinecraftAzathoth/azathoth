package com.azathoth.website.module.market.repository

import com.azathoth.website.module.market.dto.*
import com.azathoth.website.module.market.table.*
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class MarketRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * 搜索资源
     */
    suspend fun searchResources(params: SearchParams): Pair<List<ResourceListItemDTO>, Long> = dbQuery {
        val query = Resources.selectAll()
            .where { Resources.status eq "APPROVED" }

        // 关键词搜索
        params.keyword?.takeIf { it.isNotBlank() }?.let { keyword ->
            query.andWhere {
                (Resources.name like "%$keyword%") or
                (Resources.description like "%$keyword%")
            }
        }

        // 类型筛选
        params.type?.let { type ->
            query.andWhere { Resources.type eq type }
        }

        // 授权筛选
        params.license?.let { license ->
            query.andWhere { Resources.license eq license }
        }

        // 最低评分
        params.minRating?.let { minRating ->
            query.andWhere { Resources.rating greaterEq minRating }
        }

        // 标签筛选
        params.tags?.takeIf { it.isNotEmpty() }?.let { tags ->
            val resourcesWithTags = ResourceTags
                .select(ResourceTags.resourceId)
                .where { ResourceTags.tag inList tags }
                .map { it[ResourceTags.resourceId] }
            query.andWhere { Resources.id inList resourcesWithTags }
        }

        // 统计总数
        val totalCount = query.count()

        // 排序
        val sortColumn: Expression<*> = when (params.sortBy) {
            "DOWNLOADS" -> Resources.downloads
            "RATING" -> Resources.rating
            "UPDATED" -> Resources.updatedAt
            "CREATED" -> Resources.createdAt
            else -> Resources.downloads
        }

        val sortOrder = if (params.sortOrder == "ASC") SortOrder.ASC else SortOrder.DESC
        query.orderBy(sortColumn to sortOrder)

        // 分页
        val offset = ((params.page - 1) * params.pageSize).toLong()
        query.limit(params.pageSize).offset(offset)

        val resources = query.map { row ->
            val resourceId = row[Resources.id].value
            ResourceListItemDTO(
                resourceId = resourceId.toString(),
                name = row[Resources.name],
                slug = row[Resources.slug],
                description = row[Resources.description].take(200),
                type = row[Resources.type],
                license = row[Resources.license],
                authorId = row[Resources.authorId].toString(),
                authorName = row[Resources.authorName],
                latestVersion = row[Resources.latestVersion],
                pricing = getResourcePricing(resourceId),
                downloads = row[Resources.downloads],
                rating = row[Resources.rating],
                reviewCount = row[Resources.reviewCount],
                icon = row[Resources.icon],
                tags = getResourceTags(resourceId)
            )
        }

        Pair(resources, totalCount)
    }

    /**
     * 根据 ID 获取资源详情
     */
    suspend fun getResourceById(resourceId: UUID): ResourceDTO? = dbQuery {
        Resources.selectAll()
            .where { Resources.id eq resourceId }
            .singleOrNull()
            ?.let { toResourceDTO(it) }
    }

    /**
     * 根据 slug 获取资源详情
     */
    suspend fun getResourceBySlug(slug: String): ResourceDTO? = dbQuery {
        Resources.selectAll()
            .where { Resources.slug eq slug }
            .singleOrNull()
            ?.let { toResourceDTO(it) }
    }

    /**
     * 创建资源
     */
    suspend fun createResource(
        authorId: UUID,
        authorName: String,
        request: CreateResourceRequestDTO
    ): ResourceDTO? = dbQuery {
        val now = Clock.System.now().toJavaInstant()
        val slug = generateSlug(request.name)

        val resourceId = Resources.insertAndGetId {
            it[name] = request.name
            it[Resources.slug] = slug
            it[description] = request.description
            it[type] = request.type
            it[license] = request.license
            it[Resources.authorId] = authorId
            it[Resources.authorName] = authorName
            it[latestVersion] = "0.0.0"
            it[status] = "DRAFT"
            it[minApiVersion] = request.minApiVersion
            it[createdAt] = now
            it[updatedAt] = now
        }.value

        // 保存定价
        request.pricing?.let { pricing ->
            ResourcePricing.insert {
                it[ResourcePricing.resourceId] = resourceId
                it[price] = pricing.price
                it[currency] = pricing.currency
                it[subscriptionPeriod] = pricing.subscriptionPeriod
            }
        }

        // 保存标签
        request.tags.forEach { tag ->
            ResourceTags.insert {
                it[ResourceTags.resourceId] = resourceId
                it[ResourceTags.tag] = tag
            }
        }

        // 保存依赖
        request.dependencies.forEach { dep ->
            ResourceDependencies.insert {
                it[ResourceDependencies.resourceId] = resourceId
                it[dependencyId] = dep
            }
        }

        getResourceByIdInternal(resourceId)
    }

    /**
     * 更新资源
     */
    suspend fun updateResource(resourceId: UUID, request: UpdateResourceRequestDTO): ResourceDTO? = dbQuery {
        val now = Clock.System.now().toJavaInstant()

        Resources.update({ Resources.id eq resourceId }) {
            request.name?.let { name -> it[Resources.name] = name }
            request.description?.let { desc -> it[description] = desc }
            request.icon?.let { icon -> it[Resources.icon] = icon }
            it[updatedAt] = now
        }

        // 更新定价
        request.pricing?.let { pricing ->
            ResourcePricing.deleteWhere { ResourcePricing.resourceId eq resourceId }
            ResourcePricing.insert {
                it[ResourcePricing.resourceId] = resourceId
                it[price] = pricing.price
                it[currency] = pricing.currency
                it[subscriptionPeriod] = pricing.subscriptionPeriod
            }
        }

        // 更新标签
        request.tags?.let { tags ->
            ResourceTags.deleteWhere { ResourceTags.resourceId eq resourceId }
            tags.forEach { tag ->
                ResourceTags.insert {
                    it[ResourceTags.resourceId] = resourceId
                    it[ResourceTags.tag] = tag
                }
            }
        }

        // 更新截图
        request.screenshots?.let { screenshots ->
            ResourceScreenshots.deleteWhere { ResourceScreenshots.resourceId eq resourceId }
            screenshots.forEachIndexed { index, url ->
                ResourceScreenshots.insert {
                    it[ResourceScreenshots.resourceId] = resourceId
                    it[ResourceScreenshots.url] = url
                    it[sortOrder] = index
                }
            }
        }

        getResourceByIdInternal(resourceId)
    }

    /**
     * 删除资源
     */
    suspend fun deleteResource(resourceId: UUID): Boolean = dbQuery {
        // 删除相关数据
        ResourceTags.deleteWhere { ResourceTags.resourceId eq resourceId }
        ResourceScreenshots.deleteWhere { ResourceScreenshots.resourceId eq resourceId }
        ResourceDependencies.deleteWhere { ResourceDependencies.resourceId eq resourceId }
        ResourcePricing.deleteWhere { ResourcePricing.resourceId eq resourceId }
        DownloadRecords.deleteWhere { DownloadRecords.resourceId eq resourceId }
        ReviewHelpful.deleteWhere {
            ReviewHelpful.reviewId inSubQuery ResourceReviews
                .select(ResourceReviews.id)
                .where { ResourceReviews.resourceId eq resourceId }
        }
        ResourceReviews.deleteWhere { ResourceReviews.resourceId eq resourceId }
        ResourceVersions.deleteWhere { ResourceVersions.resourceId eq resourceId }

        // 删除资源
        Resources.deleteWhere { Resources.id eq resourceId } > 0
    }

    /**
     * 发布新版本
     */
    suspend fun publishVersion(
        resourceId: UUID,
        request: PublishVersionRequestDTO,
        downloadUrl: String,
        fileSize: Long
    ): ResourceVersionDTO? = dbQuery {
        val now = Clock.System.now().toJavaInstant()

        ResourceVersions.insert {
            it[ResourceVersions.resourceId] = resourceId
            it[version] = request.version
            it[changelog] = request.changelog
            it[ResourceVersions.downloadUrl] = downloadUrl
            it[ResourceVersions.fileSize] = fileSize
            it[minApiVersion] = request.minApiVersion
            it[releasedAt] = now
        }

        // 更新资源的最新版本和状态
        Resources.update({ Resources.id eq resourceId }) {
            it[latestVersion] = request.version
            it[status] = "PENDING"  // 提交审核
            it[updatedAt] = now
        }

        ResourceVersionDTO(
            version = request.version,
            changelog = request.changelog,
            downloadUrl = downloadUrl,
            fileSize = fileSize,
            minApiVersion = request.minApiVersion,
            releasedAt = now.toString()
        )
    }

    /**
     * 获取用户的资源列表
     */
    suspend fun getUserResources(userId: UUID): List<ResourceListItemDTO> = dbQuery {
        Resources.selectAll()
            .where { Resources.authorId eq userId }
            .orderBy(Resources.updatedAt to SortOrder.DESC)
            .map { row ->
                val resourceId = row[Resources.id].value
                ResourceListItemDTO(
                    resourceId = resourceId.toString(),
                    name = row[Resources.name],
                    slug = row[Resources.slug],
                    description = row[Resources.description].take(200),
                    type = row[Resources.type],
                    license = row[Resources.license],
                    authorId = row[Resources.authorId].toString(),
                    authorName = row[Resources.authorName],
                    latestVersion = row[Resources.latestVersion],
                    pricing = getResourcePricing(resourceId),
                    downloads = row[Resources.downloads],
                    rating = row[Resources.rating],
                    reviewCount = row[Resources.reviewCount],
                    icon = row[Resources.icon],
                    tags = getResourceTags(resourceId)
                )
            }
    }

    /**
     * 获取热门资源
     */
    suspend fun getPopularResources(type: String?, limit: Int): List<ResourceListItemDTO> = dbQuery {
        val query = Resources.selectAll()
            .where { Resources.status eq "APPROVED" }

        type?.let { query.andWhere { Resources.type eq it } }

        query.orderBy(Resources.downloads to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                val resourceId = row[Resources.id].value
                ResourceListItemDTO(
                    resourceId = resourceId.toString(),
                    name = row[Resources.name],
                    slug = row[Resources.slug],
                    description = row[Resources.description].take(200),
                    type = row[Resources.type],
                    license = row[Resources.license],
                    authorId = row[Resources.authorId].toString(),
                    authorName = row[Resources.authorName],
                    latestVersion = row[Resources.latestVersion],
                    pricing = getResourcePricing(resourceId),
                    downloads = row[Resources.downloads],
                    rating = row[Resources.rating],
                    reviewCount = row[Resources.reviewCount],
                    icon = row[Resources.icon],
                    tags = getResourceTags(resourceId)
                )
            }
    }

    /**
     * 获取最新资源
     */
    suspend fun getLatestResources(type: String?, limit: Int): List<ResourceListItemDTO> = dbQuery {
        val query = Resources.selectAll()
            .where { Resources.status eq "APPROVED" }

        type?.let { query.andWhere { Resources.type eq it } }

        query.orderBy(Resources.createdAt to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                val resourceId = row[Resources.id].value
                ResourceListItemDTO(
                    resourceId = resourceId.toString(),
                    name = row[Resources.name],
                    slug = row[Resources.slug],
                    description = row[Resources.description].take(200),
                    type = row[Resources.type],
                    license = row[Resources.license],
                    authorId = row[Resources.authorId].toString(),
                    authorName = row[Resources.authorName],
                    latestVersion = row[Resources.latestVersion],
                    pricing = getResourcePricing(resourceId),
                    downloads = row[Resources.downloads],
                    rating = row[Resources.rating],
                    reviewCount = row[Resources.reviewCount],
                    icon = row[Resources.icon],
                    tags = getResourceTags(resourceId)
                )
            }
    }

    // ========== 私有辅助方法 ==========

    private fun getResourceByIdInternal(resourceId: UUID): ResourceDTO? =
        Resources.selectAll()
            .where { Resources.id eq resourceId }
            .singleOrNull()
            ?.let { toResourceDTO(it) }

    private fun toResourceDTO(row: ResultRow): ResourceDTO {
        val resourceId = row[Resources.id].value
        return ResourceDTO(
            resourceId = resourceId.toString(),
            name = row[Resources.name],
            slug = row[Resources.slug],
            description = row[Resources.description],
            type = row[Resources.type],
            license = row[Resources.license],
            authorId = row[Resources.authorId].toString(),
            authorName = row[Resources.authorName],
            versions = getResourceVersions(resourceId),
            latestVersion = row[Resources.latestVersion],
            pricing = getResourcePricing(resourceId),
            downloads = row[Resources.downloads],
            rating = row[Resources.rating],
            reviewCount = row[Resources.reviewCount],
            status = row[Resources.status],
            minApiVersion = row[Resources.minApiVersion],
            maxApiVersion = row[Resources.maxApiVersion],
            dependencies = getResourceDependencies(resourceId),
            icon = row[Resources.icon],
            screenshots = getResourceScreenshots(resourceId),
            tags = getResourceTags(resourceId),
            createdAt = row[Resources.createdAt].toString(),
            updatedAt = row[Resources.updatedAt].toString()
        )
    }

    private fun getResourceVersions(resourceId: UUID): List<ResourceVersionDTO> =
        ResourceVersions.selectAll()
            .where { ResourceVersions.resourceId eq resourceId }
            .orderBy(ResourceVersions.releasedAt to SortOrder.DESC)
            .map {
                ResourceVersionDTO(
                    version = it[ResourceVersions.version],
                    changelog = it[ResourceVersions.changelog],
                    downloadUrl = it[ResourceVersions.downloadUrl],
                    fileSize = it[ResourceVersions.fileSize],
                    minApiVersion = it[ResourceVersions.minApiVersion],
                    releasedAt = it[ResourceVersions.releasedAt].toString()
                )
            }

    private fun getResourcePricing(resourceId: UUID): ResourcePricingDTO? =
        ResourcePricing.selectAll()
            .where { ResourcePricing.resourceId eq resourceId }
            .singleOrNull()
            ?.let {
                ResourcePricingDTO(
                    price = it[ResourcePricing.price],
                    currency = it[ResourcePricing.currency],
                    subscriptionPeriod = it[ResourcePricing.subscriptionPeriod]
                )
            }

    private fun getResourceTags(resourceId: UUID): List<String> =
        ResourceTags.selectAll()
            .where { ResourceTags.resourceId eq resourceId }
            .map { it[ResourceTags.tag] }

    private fun getResourceScreenshots(resourceId: UUID): List<String> =
        ResourceScreenshots.selectAll()
            .where { ResourceScreenshots.resourceId eq resourceId }
            .orderBy(ResourceScreenshots.sortOrder to SortOrder.ASC)
            .map { it[ResourceScreenshots.url] }

    private fun getResourceDependencies(resourceId: UUID): List<String> =
        ResourceDependencies.selectAll()
            .where { ResourceDependencies.resourceId eq resourceId }
            .map { it[ResourceDependencies.dependencyId] }

    private fun generateSlug(name: String): String {
        val base = name.lowercase()
            .replace(Regex("[^a-z0-9\\u4e00-\\u9fa5]+"), "-")
            .trim('-')
        val suffix = UUID.randomUUID().toString().take(8)
        return "$base-$suffix"
    }
}
