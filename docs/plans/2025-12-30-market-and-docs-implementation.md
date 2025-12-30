# Azathoth 开发者市场模块 & 前端文档页面 实施计划

> 创建日期: 2025-12-30
> 目标: 为没有代码库上下文的工程师提供详细的实施指南

---

## 目录

1. [项目概述](#1-项目概述)
2. [后端实施计划 - 开发者市场模块](#2-后端实施计划---开发者市场模块)
3. [前端实施计划 - 开发者市场模块](#3-前端实施计划---开发者市场模块)
4. [前端实施计划 - Docs 文档页面](#4-前端实施计划---docs-文档页面)
5. [验证步骤](#5-验证步骤)

---

## 1. 项目概述

### 1.1 技术栈

**后端:**
- Kotlin 2.3.0
- Ktor 3.3.3 (Web 框架)
- Exposed (ORM)
- PostgreSQL (关系型数据库)
- MongoDB (文档存储)
- Redis/Lettuce (缓存)

**前端:**
- Nuxt 3 (Vue 3 + SSR)
- TypeScript
- Tailwind CSS
- Pinia (状态管理)
- @nuxt/content (Markdown 文档)

### 1.2 现有代码结构

```
azathoth/
├── website/
│   ├── backend/                          # Ktor 后端
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/com/azathoth/website/
│   │       ├── WebsiteMain.kt            # 入口点
│   │       └── module/
│   │           ├── auth/                 # 认证模块 (接口已定义)
│   │           ├── market/               # 市场模块 (接口已定义)
│   │           ├── payment/              # 支付模块 (接口已定义)
│   │           ├── forum/                # 论坛模块 (接口已定义)
│   │           ├── generator/            # 生成器模块 (接口已定义)
│   │           └── review/               # 审核模块 (接口已定义)
│   └── frontend/                         # Nuxt 3 前端
│       ├── nuxt.config.ts
│       ├── pages/
│       │   ├── market/
│       │   │   ├── index.vue             # 市场列表页 (已有基础)
│       │   │   └── [slug].vue            # 资源详情页 (已有基础)
│       │   └── wiki/
│       │       ├── index.vue             # 文档首页 (已有基础)
│       │       └── [...slug].vue         # 文档内容页 (已有基础)
│       ├── components/
│       │   └── ResourceCard.vue          # 资源卡片组件 (已有)
│       ├── stores/
│       │   ├── auth.ts                   # 认证状态
│       │   └── market.ts                 # 市场状态 (已有基础)
│       ├── types/
│       │   └── index.ts                  # TypeScript 类型定义 (已有)
│       ├── layouts/
│       │   ├── default.vue
│       │   └── wiki.vue                  # 文档布局 (已有基础)
│       └── content/
│           └── wiki/api/                 # API 文档 (已有基础)
```

### 1.3 已定义的接口 (待实现)

后端已定义以下 Kotlin 接口，但尚未实现：

- `MarketService` - 市场核心服务
- `ReviewService` - 评论服务
- `DownloadService` - 下载服务

---

## 2. 后端实施计划 - 开发者市场模块

### 任务 2.1: 创建数据库表定义

**文件路径:** `website/backend/src/main/kotlin/com/azathoth/website/module/market/table/`

#### 2.1.1 创建 MarketTables.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/table/MarketTables.kt
package com.azathoth.website.module.market.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * 资源表
 */
object Resources : UUIDTable("market_resources") {
    val name = varchar("name", 100)
    val slug = varchar("slug", 100).uniqueIndex()
    val description = text("description")
    val type = varchar("type", 20)  // PLUGIN, MODULE, SERVICE, TEMPLATE, THEME, TOOL
    val license = varchar("license", 30)  // FREE_OPEN_SOURCE, FREE_CLOSED_SOURCE, PAID_PERPETUAL, PAID_SUBSCRIPTION
    val authorId = uuid("author_id").index()
    val authorName = varchar("author_name", 50)
    val latestVersion = varchar("latest_version", 20)
    val downloads = long("downloads").default(0)
    val rating = double("rating").default(0.0)
    val reviewCount = integer("review_count").default(0)
    val status = varchar("status", 20).default("DRAFT")  // DRAFT, PENDING, APPROVED, REJECTED, SUSPENDED
    val minApiVersion = varchar("min_api_version", 20)
    val maxApiVersion = varchar("max_api_version", 20).nullable()
    val icon = varchar("icon", 500).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

/**
 * 资源版本表
 */
object ResourceVersions : UUIDTable("market_resource_versions") {
    val resourceId = uuid("resource_id").references(Resources.id).index()
    val version = varchar("version", 20)
    val changelog = text("changelog")
    val downloadUrl = varchar("download_url", 500)
    val fileSize = long("file_size")
    val minApiVersion = varchar("min_api_version", 20)
    val releasedAt = timestamp("released_at")

    init {
        uniqueIndex(resourceId, version)
    }
}

/**
 * 资源定价表
 */
object ResourcePricing : Table("market_resource_pricing") {
    val resourceId = uuid("resource_id").references(Resources.id)
    val price = long("price")  // 分为单位
    val currency = varchar("currency", 10).default("CNY")
    val subscriptionPeriod = integer("subscription_period").nullable()  // 天数，null表示买断

    override val primaryKey = PrimaryKey(resourceId)
}

/**
 * 资源标签关联表
 */
object ResourceTags : Table("market_resource_tags") {
    val resourceId = uuid("resource_id").references(Resources.id)
    val tag = varchar("tag", 50)

    override val primaryKey = PrimaryKey(resourceId, tag)
}

/**
 * 资源截图表
 */
object ResourceScreenshots : Table("market_resource_screenshots") {
    val id = uuid("id").autoGenerate()
    val resourceId = uuid("resource_id").references(Resources.id).index()
    val url = varchar("url", 500)
    val sortOrder = integer("sort_order").default(0)

    override val primaryKey = PrimaryKey(id)
}

/**
 * 资源依赖表
 */
object ResourceDependencies : Table("market_resource_dependencies") {
    val resourceId = uuid("resource_id").references(Resources.id)
    val dependencyId = varchar("dependency_id", 100)  // 依赖的资源ID或外部依赖名

    override val primaryKey = PrimaryKey(resourceId, dependencyId)
}

/**
 * 资源评论表
 */
object ResourceReviews : UUIDTable("market_resource_reviews") {
    val resourceId = uuid("resource_id").references(Resources.id).index()
    val userId = uuid("user_id").index()
    val userName = varchar("user_name", 50)
    val rating = integer("rating")  // 1-5
    val content = text("content")
    val helpful = integer("helpful").default(0)
    val authorReply = text("author_reply").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at").nullable()

    init {
        uniqueIndex(resourceId, userId)  // 每个用户对每个资源只能评论一次
    }
}

/**
 * 评论有帮助记录表
 */
object ReviewHelpful : Table("market_review_helpful") {
    val reviewId = uuid("review_id").references(ResourceReviews.id)
    val userId = uuid("user_id")

    override val primaryKey = PrimaryKey(reviewId, userId)
}

/**
 * 下载记录表
 */
object DownloadRecords : UUIDTable("market_download_records") {
    val resourceId = uuid("resource_id").references(Resources.id).index()
    val version = varchar("version", 20)
    val userId = uuid("user_id").nullable().index()
    val ipAddress = varchar("ip_address", 45).nullable()
    val downloadedAt = timestamp("downloaded_at")
}

/**
 * 用户购买记录表
 */
object UserPurchases : Table("market_user_purchases") {
    val userId = uuid("user_id")
    val resourceId = uuid("resource_id").references(Resources.id)
    val orderId = varchar("order_id", 50)
    val purchasedAt = timestamp("purchased_at")
    val expiresAt = timestamp("expires_at").nullable()  // 订阅到期时间，null表示永久

    override val primaryKey = PrimaryKey(userId, resourceId)
}
```

#### 验证步骤:
```bash
# 在项目根目录执行
./gradlew :website:backend:compileKotlin
```

---

### 任务 2.2: 实现数据传输对象 (DTO)

**文件路径:** `website/backend/src/main/kotlin/com/azathoth/website/module/market/dto/`

#### 2.2.1 创建 MarketDTOs.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/dto/MarketDTOs.kt
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
    val subscriptionPeriod: Int?
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
```

#### 2.2.2 创建 MarketRequests.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/dto/MarketRequests.kt
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
    val pricing: ResourcePricingDTO?,
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
```

#### 2.2.3 创建 MarketResponses.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/dto/MarketResponses.kt
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
```

---

### 任务 2.3: 实现仓储层 (Repository)

**文件路径:** `website/backend/src/main/kotlin/com/azathoth/website/module/market/repository/`

#### 2.3.1 创建 MarketRepository.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/repository/MarketRepository.kt
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
        params.keyword?.let { keyword ->
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
        val sortColumn = when (params.sortBy) {
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

        getResourceById(resourceId)
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

        getResourceById(resourceId)
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

        // 更新资源的最新版本
        Resources.update({ Resources.id eq resourceId }) {
            it[latestVersion] = request.version
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
```

#### 2.3.2 创建 ReviewRepository.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/repository/ReviewRepository.kt
package com.azathoth.website.module.market.repository

import com.azathoth.website.module.market.dto.*
import com.azathoth.website.module.market.table.*
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class ReviewRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    /**
     * 获取资源评论
     */
    suspend fun getReviews(resourceId: UUID, page: Int, pageSize: Int): Pair<List<ResourceReviewDTO>, Long> = dbQuery {
        val totalCount = ResourceReviews.selectAll()
            .where { ResourceReviews.resourceId eq resourceId }
            .count()

        val offset = ((page - 1) * pageSize).toLong()
        val reviews = ResourceReviews.selectAll()
            .where { ResourceReviews.resourceId eq resourceId }
            .orderBy(ResourceReviews.createdAt to SortOrder.DESC)
            .limit(pageSize).offset(offset)
            .map { toReviewDTO(it) }

        Pair(reviews, totalCount)
    }

    /**
     * 创建评论
     */
    suspend fun createReview(
        resourceId: UUID,
        userId: UUID,
        userName: String,
        rating: Int,
        content: String
    ): ResourceReviewDTO? = dbQuery {
        // 检查是否已评论
        val existing = ResourceReviews.selectAll()
            .where { (ResourceReviews.resourceId eq resourceId) and (ResourceReviews.userId eq userId) }
            .singleOrNull()

        if (existing != null) return@dbQuery null

        val now = Clock.System.now().toJavaInstant()
        val reviewId = ResourceReviews.insertAndGetId {
            it[ResourceReviews.resourceId] = resourceId
            it[ResourceReviews.userId] = userId
            it[ResourceReviews.userName] = userName
            it[ResourceReviews.rating] = rating
            it[ResourceReviews.content] = content
            it[createdAt] = now
        }.value

        // 更新资源评分和评论数
        updateResourceRating(resourceId)

        ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull()
            ?.let { toReviewDTO(it) }
    }

    /**
     * 更新评论
     */
    suspend fun updateReview(reviewId: UUID, rating: Int?, content: String?): ResourceReviewDTO? = dbQuery {
        val now = Clock.System.now().toJavaInstant()

        val review = ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull() ?: return@dbQuery null

        ResourceReviews.update({ ResourceReviews.id eq reviewId }) {
            rating?.let { r -> it[ResourceReviews.rating] = r }
            content?.let { c -> it[ResourceReviews.content] = c }
            it[updatedAt] = now
        }

        // 更新资源评分
        val resourceId = review[ResourceReviews.resourceId]
        updateResourceRating(resourceId)

        ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull()
            ?.let { toReviewDTO(it) }
    }

    /**
     * 删除评论
     */
    suspend fun deleteReview(reviewId: UUID): Boolean = dbQuery {
        val review = ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull() ?: return@dbQuery false

        val resourceId = review[ResourceReviews.resourceId]

        ReviewHelpful.deleteWhere { ReviewHelpful.reviewId eq reviewId }
        val deleted = ResourceReviews.deleteWhere { ResourceReviews.id eq reviewId } > 0

        if (deleted) {
            updateResourceRating(resourceId)
        }

        deleted
    }

    /**
     * 作者回复
     */
    suspend fun replyToReview(reviewId: UUID, reply: String): ResourceReviewDTO? = dbQuery {
        val now = Clock.System.now().toJavaInstant()

        ResourceReviews.update({ ResourceReviews.id eq reviewId }) {
            it[authorReply] = reply
            it[updatedAt] = now
        }

        ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull()
            ?.let { toReviewDTO(it) }
    }

    /**
     * 标记评论有帮助
     */
    suspend fun markHelpful(reviewId: UUID, userId: UUID): Boolean = dbQuery {
        // 检查是否已标记
        val existing = ReviewHelpful.selectAll()
            .where { (ReviewHelpful.reviewId eq reviewId) and (ReviewHelpful.userId eq userId) }
            .singleOrNull()

        if (existing != null) return@dbQuery false

        ReviewHelpful.insert {
            it[ReviewHelpful.reviewId] = reviewId
            it[ReviewHelpful.userId] = userId
        }

        ResourceReviews.update({ ResourceReviews.id eq reviewId }) {
            it[helpful] = helpful + 1
        }

        true
    }

    /**
     * 获取评论所属资源的作者ID
     */
    suspend fun getReviewResourceAuthorId(reviewId: UUID): UUID? = dbQuery {
        val review = ResourceReviews.selectAll()
            .where { ResourceReviews.id eq reviewId }
            .singleOrNull() ?: return@dbQuery null

        val resourceId = review[ResourceReviews.resourceId]

        Resources.selectAll()
            .where { Resources.id eq resourceId }
            .singleOrNull()
            ?.get(Resources.authorId)
    }

    // ========== 私有辅助方法 ==========

    private fun toReviewDTO(row: ResultRow): ResourceReviewDTO =
        ResourceReviewDTO(
            reviewId = row[ResourceReviews.id].value.toString(),
            resourceId = row[ResourceReviews.resourceId].toString(),
            userId = row[ResourceReviews.userId].toString(),
            userName = row[ResourceReviews.userName],
            rating = row[ResourceReviews.rating],
            content = row[ResourceReviews.content],
            createdAt = row[ResourceReviews.createdAt].toString(),
            updatedAt = row[ResourceReviews.updatedAt]?.toString(),
            helpful = row[ResourceReviews.helpful],
            authorReply = row[ResourceReviews.authorReply]
        )

    private fun updateResourceRating(resourceId: UUID) {
        val stats = ResourceReviews.select(
            ResourceReviews.rating.avg(),
            ResourceReviews.rating.count()
        ).where { ResourceReviews.resourceId eq resourceId }
            .single()

        val avgRating = stats[ResourceReviews.rating.avg()]?.toDouble() ?: 0.0
        val count = stats[ResourceReviews.rating.count()].toInt()

        Resources.update({ Resources.id eq resourceId }) {
            it[rating] = avgRating
            it[reviewCount] = count
        }
    }
}
```

---

### 任务 2.4: 实现服务层 (Service Implementation)

**文件路径:** `website/backend/src/main/kotlin/com/azathoth/website/module/market/impl/`

#### 2.4.1 创建 MarketServiceImpl.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/impl/MarketServiceImpl.kt
package com.azathoth.website.module.market.impl

import com.azathoth.website.module.market.*
import com.azathoth.website.module.market.dto.*
import com.azathoth.website.module.market.repository.MarketRepository
import java.time.Instant
import java.util.*

class MarketServiceImpl(
    private val repository: MarketRepository
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

        val (resources, totalCount) = repository.searchResources(params)
        val totalPages = ((totalCount + criteria.pageSize - 1) / criteria.pageSize).toInt()

        return object : MarketSearchResult {
            override val resources: List<MarketResource> = resources.map { it.toMarketResource() }
            override val totalCount: Long = totalCount
            override val page: Int = criteria.page
            override val totalPages: Int = totalPages
        }
    }

    override suspend fun getResource(resourceId: String): MarketResource? {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return null }
        return repository.getResourceById(uuid)?.toMarketResource()
    }

    override suspend fun getResourceBySlug(slug: String): MarketResource? {
        return repository.getResourceBySlug(slug)?.toMarketResource()
    }

    override suspend fun createResource(authorId: String, resource: CreateResourceRequest): MarketResource? {
        val uuid = try { UUID.fromString(authorId) } catch (e: Exception) { return null }

        val request = CreateResourceRequestDTO(
            name = resource.name,
            description = resource.description,
            type = resource.type.name,
            license = resource.license.name,
            pricing = resource.pricing?.let {
                ResourcePricingDTO(it.price, it.currency, it.subscriptionPeriod)
            },
            minApiVersion = resource.minApiVersion,
            dependencies = resource.dependencies,
            tags = resource.tags
        )

        // 注: 实际实现中需要从认证上下文获取 authorName
        return repository.createResource(uuid, "Unknown", request)?.toMarketResource()
    }

    override suspend fun updateResource(resourceId: String, update: UpdateResourceRequest): MarketResource? {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return null }

        val request = UpdateResourceRequestDTO(
            name = update.name,
            description = update.description,
            pricing = update.pricing?.let {
                ResourcePricingDTO(it.price, it.currency, it.subscriptionPeriod)
            },
            icon = update.icon,
            screenshots = update.screenshots,
            tags = update.tags
        )

        return repository.updateResource(uuid, request)?.toMarketResource()
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

        return repository.publishVersion(uuid, request, downloadUrl, fileSize)?.toResourceVersion()
    }

    override suspend fun deleteResource(resourceId: String): Boolean {
        val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) { return false }
        return repository.deleteResource(uuid)
    }

    override suspend fun getUserResources(userId: String): List<MarketResource> {
        val uuid = try { UUID.fromString(userId) } catch (e: Exception) { return emptyList() }
        return repository.getUserResources(uuid).map { it.toMarketResource() }
    }

    override suspend fun getPopularResources(type: ResourceType?, limit: Int): List<MarketResource> {
        return repository.getPopularResources(type?.name, limit).map { it.toMarketResource() }
    }

    override suspend fun getLatestResources(type: ResourceType?, limit: Int): List<MarketResource> {
        return repository.getLatestResources(type?.name, limit).map { it.toMarketResource() }
    }

    // ========== 扩展函数 ==========

    private fun ResourceDTO.toMarketResource(): MarketResource = object : MarketResource {
        override val resourceId: String = this@toMarketResource.resourceId
        override val name: String = this@toMarketResource.name
        override val slug: String = this@toMarketResource.slug
        override val description: String = this@toMarketResource.description
        override val type: ResourceType = ResourceType.valueOf(this@toMarketResource.type)
        override val license: LicenseType = LicenseType.valueOf(this@toMarketResource.license)
        override val authorId: String = this@toMarketResource.authorId
        override val authorName: String = this@toMarketResource.authorName
        override val versions: List<ResourceVersion> = this@toMarketResource.versions.map { it.toResourceVersion() }
        override val latestVersion: String = this@toMarketResource.latestVersion
        override val pricing: ResourcePricing? = this@toMarketResource.pricing?.toResourcePricing()
        override val downloads: Long = this@toMarketResource.downloads
        override val rating: Double = this@toMarketResource.rating
        override val reviewCount: Int = this@toMarketResource.reviewCount
        override val status: ResourceStatus = ResourceStatus.valueOf(this@toMarketResource.status)
        override val minApiVersion: String = this@toMarketResource.minApiVersion
        override val maxApiVersion: String? = this@toMarketResource.maxApiVersion
        override val dependencies: List<String> = this@toMarketResource.dependencies
        override val icon: String = this@toMarketResource.icon ?: ""
        override val screenshots: List<String> = this@toMarketResource.screenshots
        override val tags: List<String> = this@toMarketResource.tags
        override val createdAt: Instant = Instant.parse(this@toMarketResource.createdAt)
        override val updatedAt: Instant = Instant.parse(this@toMarketResource.updatedAt)
    }

    private fun ResourceListItemDTO.toMarketResource(): MarketResource = object : MarketResource {
        override val resourceId: String = this@toMarketResource.resourceId
        override val name: String = this@toMarketResource.name
        override val slug: String = this@toMarketResource.slug
        override val description: String = this@toMarketResource.description
        override val type: ResourceType = ResourceType.valueOf(this@toMarketResource.type)
        override val license: LicenseType = LicenseType.valueOf(this@toMarketResource.license)
        override val authorId: String = this@toMarketResource.authorId
        override val authorName: String = this@toMarketResource.authorName
        override val versions: List<ResourceVersion> = emptyList()
        override val latestVersion: String = this@toMarketResource.latestVersion
        override val pricing: ResourcePricing? = this@toMarketResource.pricing?.toResourcePricing()
        override val downloads: Long = this@toMarketResource.downloads
        override val rating: Double = this@toMarketResource.rating
        override val reviewCount: Int = this@toMarketResource.reviewCount
        override val status: ResourceStatus = ResourceStatus.APPROVED
        override val minApiVersion: String = ""
        override val maxApiVersion: String? = null
        override val dependencies: List<String> = emptyList()
        override val icon: String = this@toMarketResource.icon ?: ""
        override val screenshots: List<String> = emptyList()
        override val tags: List<String> = this@toMarketResource.tags
        override val createdAt: Instant = Instant.now()
        override val updatedAt: Instant = Instant.now()
    }

    private fun ResourceVersionDTO.toResourceVersion(): ResourceVersion = object : ResourceVersion {
        override val version: String = this@toResourceVersion.version
        override val changelog: String = this@toResourceVersion.changelog
        override val downloadUrl: String = this@toResourceVersion.downloadUrl
        override val fileSize: Long = this@toResourceVersion.fileSize
        override val minApiVersion: String = this@toResourceVersion.minApiVersion
        override val releasedAt: Instant = Instant.parse(this@toResourceVersion.releasedAt)
    }

    private fun ResourcePricingDTO.toResourcePricing(): ResourcePricing = object : ResourcePricing {
        override val price: Long = this@toResourcePricing.price
        override val currency: String = this@toResourcePricing.currency
        override val subscriptionPeriod: Int? = this@toResourcePricing.subscriptionPeriod
    }
}
```

---

### 任务 2.5: 实现 HTTP 路由层 (Routes)

**文件路径:** `website/backend/src/main/kotlin/com/azathoth/website/module/market/routes/`

#### 2.5.1 创建 MarketRoutes.kt

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/module/market/routes/MarketRoutes.kt
package com.azathoth.website.module.market.routes

import com.azathoth.website.module.market.*
import com.azathoth.website.module.market.dto.*
import com.azathoth.website.module.market.impl.MarketServiceImpl
import com.azathoth.website.module.market.repository.MarketRepository
import com.azathoth.website.module.market.repository.ReviewRepository
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureMarketRoutes() {
    val marketRepository = MarketRepository()
    val reviewRepository = ReviewRepository()
    val marketService = MarketServiceImpl(marketRepository)

    routing {
        route("/api/market") {
            // ========== 公开接口 ==========

            // 搜索资源
            get("/resources") {
                val params = SearchParams(
                    keyword = call.parameters["keyword"],
                    type = call.parameters["type"],
                    license = call.parameters["license"],
                    minRating = call.parameters["minRating"]?.toDoubleOrNull(),
                    tags = call.parameters["tags"]?.split(",")?.filter { it.isNotBlank() },
                    sortBy = call.parameters["sortBy"] ?: "DOWNLOADS",
                    sortOrder = call.parameters["sortOrder"] ?: "DESC",
                    page = call.parameters["page"]?.toIntOrNull() ?: 1,
                    pageSize = call.parameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
                )

                val (resources, totalCount) = marketRepository.searchResources(params)
                val totalPages = ((totalCount + params.pageSize - 1) / params.pageSize).toInt()

                call.respond(SearchResourcesResponse(
                    success = true,
                    resources = resources,
                    pagination = PaginationDTO(
                        page = params.page,
                        pageSize = params.pageSize,
                        totalCount = totalCount,
                        totalPages = totalPages
                    )
                ))
            }

            // 获取热门资源
            get("/popular") {
                val type = call.parameters["type"]
                val limit = call.parameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 10

                val resources = marketRepository.getPopularResources(type, limit)
                call.respond(ResourceListResponse(success = true, resources = resources))
            }

            // 获取最新资源
            get("/latest") {
                val type = call.parameters["type"]
                val limit = call.parameters["limit"]?.toIntOrNull()?.coerceIn(1, 50) ?: 10

                val resources = marketRepository.getLatestResources(type, limit)
                call.respond(ResourceListResponse(success = true, resources = resources))
            }

            // 根据 ID 获取资源详情
            get("/resources/{resourceId}") {
                val resourceId = call.parameters["resourceId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ResourceResponse(success = false, error = ErrorDTO("INVALID_ID", "资源ID无效"))
                )

                val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ResourceResponse(success = false, error = ErrorDTO("INVALID_ID", "资源ID格式错误"))
                    )
                }

                val resource = marketRepository.getResourceById(uuid)
                if (resource != null) {
                    call.respond(ResourceResponse(success = true, resource = resource))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ResourceResponse(success = false, error = ErrorDTO("NOT_FOUND", "资源不存在"))
                    )
                }
            }

            // 根据 slug 获取资源详情
            get("/resources/slug/{slug}") {
                val slug = call.parameters["slug"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ResourceResponse(success = false, error = ErrorDTO("INVALID_SLUG", "Slug无效"))
                )

                val resource = marketRepository.getResourceBySlug(slug)
                if (resource != null) {
                    call.respond(ResourceResponse(success = true, resource = resource))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ResourceResponse(success = false, error = ErrorDTO("NOT_FOUND", "资源不存在"))
                    )
                }
            }

            // 获取资源评论
            get("/resources/{resourceId}/reviews") {
                val resourceId = call.parameters["resourceId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ReviewListResponse(
                        success = false,
                        reviews = emptyList(),
                        pagination = PaginationDTO(1, 20, 0, 0)
                    )
                )

                val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                    return@get call.respond(HttpStatusCode.BadRequest)
                }

                val page = call.parameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.parameters["pageSize"]?.toIntOrNull()?.coerceIn(1, 50) ?: 20

                val (reviews, totalCount) = reviewRepository.getReviews(uuid, page, pageSize)
                val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt()

                call.respond(ReviewListResponse(
                    success = true,
                    reviews = reviews,
                    pagination = PaginationDTO(page, pageSize, totalCount, totalPages)
                ))
            }

            // 获取用户资源列表
            get("/users/{userId}/resources") {
                val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val uuid = try { UUID.fromString(userId) } catch (e: Exception) {
                    return@get call.respond(HttpStatusCode.BadRequest)
                }

                val resources = marketRepository.getUserResources(uuid)
                call.respond(ResourceListResponse(success = true, resources = resources))
            }

            // ========== 需要认证的接口 ==========

            authenticate("jwt") {
                // 创建资源
                post("/resources") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    val userName = principal.payload.getClaim("userName")?.asString() ?: "Unknown"

                    val request = call.receive<CreateResourceRequestDTO>()
                    val uuid = UUID.fromString(userId)

                    val resource = marketRepository.createResource(uuid, userName, request)
                    if (resource != null) {
                        call.respond(HttpStatusCode.Created, ResourceResponse(success = true, resource = resource))
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ResourceResponse(success = false, error = ErrorDTO("CREATE_FAILED", "创建资源失败"))
                        )
                    }
                }

                // 更新资源
                patch("/resources/{resourceId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@patch call.respond(HttpStatusCode.Unauthorized)

                    val resourceId = call.parameters["resourceId"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
                    val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@patch call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证所有权
                    val existing = marketRepository.getResourceById(uuid)
                    if (existing == null) {
                        return@patch call.respond(
                            HttpStatusCode.NotFound,
                            ResourceResponse(success = false, error = ErrorDTO("NOT_FOUND", "资源不存在"))
                        )
                    }
                    if (existing.authorId != userId) {
                        return@patch call.respond(
                            HttpStatusCode.Forbidden,
                            ResourceResponse(success = false, error = ErrorDTO("FORBIDDEN", "无权修改此资源"))
                        )
                    }

                    val request = call.receive<UpdateResourceRequestDTO>()
                    val updated = marketRepository.updateResource(uuid, request)

                    if (updated != null) {
                        call.respond(ResourceResponse(success = true, resource = updated))
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ResourceResponse(success = false, error = ErrorDTO("UPDATE_FAILED", "更新资源失败"))
                        )
                    }
                }

                // 删除资源
                delete("/resources/{resourceId}") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                    val resourceId = call.parameters["resourceId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@delete call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证所有权
                    val existing = marketRepository.getResourceById(uuid)
                    if (existing == null) {
                        return@delete call.respond(HttpStatusCode.NotFound)
                    }
                    if (existing.authorId != userId) {
                        return@delete call.respond(HttpStatusCode.Forbidden)
                    }

                    val deleted = marketRepository.deleteResource(uuid)
                    call.respond(SimpleResponse(success = deleted))
                }

                // 发布新版本 (multipart)
                post("/resources/{resourceId}/versions") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val resourceId = call.parameters["resourceId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val uuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证所有权
                    val existing = marketRepository.getResourceById(uuid)
                    if (existing == null) {
                        return@post call.respond(HttpStatusCode.NotFound)
                    }
                    if (existing.authorId != userId) {
                        return@post call.respond(HttpStatusCode.Forbidden)
                    }

                    var version: String? = null
                    var changelog: String? = null
                    var minApiVersion: String? = null
                    var fileBytes: ByteArray? = null

                    val multipart = call.receiveMultipart()
                    multipart.forEachPart { part ->
                        when (part) {
                            is PartData.FormItem -> {
                                when (part.name) {
                                    "version" -> version = part.value
                                    "changelog" -> changelog = part.value
                                    "minApiVersion" -> minApiVersion = part.value
                                }
                            }
                            is PartData.FileItem -> {
                                if (part.name == "file") {
                                    fileBytes = part.streamProvider().readBytes()
                                }
                            }
                            else -> {}
                        }
                        part.dispose()
                    }

                    if (version == null || changelog == null || minApiVersion == null || fileBytes == null) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            VersionResponse(success = false, error = ErrorDTO("MISSING_FIELDS", "缺少必要字段"))
                        )
                    }

                    // TODO: 上传文件到对象存储
                    val downloadUrl = "https://cdn.azathoth.dev/resources/$resourceId/$version/plugin.jar"

                    val request = PublishVersionRequestDTO(version!!, changelog!!, minApiVersion!!)
                    val versionDTO = marketRepository.publishVersion(uuid, request, downloadUrl, fileBytes!!.size.toLong())

                    if (versionDTO != null) {
                        call.respond(HttpStatusCode.Created, VersionResponse(success = true, version = versionDTO))
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            VersionResponse(success = false, error = ErrorDTO("PUBLISH_FAILED", "发布版本失败"))
                        )
                    }
                }

                // 创建评论
                post("/resources/{resourceId}/reviews") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)
                    val userName = principal.payload.getClaim("userName")?.asString() ?: "Unknown"

                    val resourceId = call.parameters["resourceId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val resourceUuid = try { UUID.fromString(resourceId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }
                    val userUuid = UUID.fromString(userId)

                    val request = call.receive<CreateReviewRequestDTO>()

                    if (request.rating !in 1..5) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ReviewResponse(success = false, error = ErrorDTO("INVALID_RATING", "评分必须在1-5之间"))
                        )
                    }

                    val review = reviewRepository.createReview(
                        resourceUuid, userUuid, userName, request.rating, request.content
                    )

                    if (review != null) {
                        call.respond(HttpStatusCode.Created, ReviewResponse(success = true, review = review))
                    } else {
                        call.respond(
                            HttpStatusCode.Conflict,
                            ReviewResponse(success = false, error = ErrorDTO("ALREADY_REVIEWED", "您已评论过此资源"))
                        )
                    }
                }

                // 回复评论 (仅资源作者)
                post("/reviews/{reviewId}/reply") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val reviewId = call.parameters["reviewId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val reviewUuid = try { UUID.fromString(reviewId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }

                    // 验证是否为资源作者
                    val authorId = reviewRepository.getReviewResourceAuthorId(reviewUuid)
                    if (authorId?.toString() != userId) {
                        return@post call.respond(
                            HttpStatusCode.Forbidden,
                            ReviewResponse(success = false, error = ErrorDTO("FORBIDDEN", "只有资源作者可以回复评论"))
                        )
                    }

                    val request = call.receive<ReplyReviewRequestDTO>()
                    val review = reviewRepository.replyToReview(reviewUuid, request.reply)

                    if (review != null) {
                        call.respond(ReviewResponse(success = true, review = review))
                    } else {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ReviewResponse(success = false, error = ErrorDTO("NOT_FOUND", "评论不存在"))
                        )
                    }
                }

                // 标记评论有帮助
                post("/reviews/{reviewId}/helpful") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized)

                    val reviewId = call.parameters["reviewId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val reviewUuid = try { UUID.fromString(reviewId) } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest)
                    }
                    val userUuid = UUID.fromString(userId)

                    val success = reviewRepository.markHelpful(reviewUuid, userUuid)
                    call.respond(SimpleResponse(success = success))
                }
            }
        }
    }
}
```

---

### 任务 2.6: 更新主入口文件

**文件路径:** `website/backend/src/main/kotlin/com/azathoth/website/WebsiteMain.kt`

```kotlin
// 文件: website/backend/src/main/kotlin/com/azathoth/website/WebsiteMain.kt
package com.azathoth.website

import com.azathoth.website.module.market.routes.configureMarketRoutes
import com.azathoth.website.module.market.table.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting Azathoth Website Backend" }

    embeddedServer(Netty, port = 8080) {
        configurePlugins()
        configureSecurity()
        configureDatabase()
        configureRouting()
        configureMarketRoutes()
    }.start(wait = true)
}

fun Application.configurePlugins() {
    // JSON 序列化
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    // CORS
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    // 错误处理
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception" }
            call.respondText(
                text = """{"success": false, "error": {"code": "INTERNAL_ERROR", "message": "${cause.message}"}}""",
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.InternalServerError
            )
        }
    }
}

fun Application.configureSecurity() {
    val jwtSecret = environment.config.propertyOrNull("jwt.secret")?.getString() ?: "dev-secret-key"
    val jwtIssuer = environment.config.propertyOrNull("jwt.issuer")?.getString() ?: "azathoth"
    val jwtRealm = environment.config.propertyOrNull("jwt.realm")?.getString() ?: "azathoth"

    install(Authentication) {
        jwt("jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("userId").asString() != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token invalid or expired"))
            }
        }
    }
}

fun Application.configureDatabase() {
    val jdbcUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:postgresql://localhost:5432/azathoth"
    val user = environment.config.propertyOrNull("database.user")?.getString() ?: "azathoth"
    val password = environment.config.propertyOrNull("database.password")?.getString() ?: "azathoth"

    Database.connect(
        url = jdbcUrl,
        driver = "org.postgresql.Driver",
        user = user,
        password = password
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            Resources,
            ResourceVersions,
            ResourcePricing,
            ResourceTags,
            ResourceScreenshots,
            ResourceDependencies,
            ResourceReviews,
            ReviewHelpful,
            DownloadRecords,
            UserPurchases
        )
    }

    logger.info { "Database connected and tables created" }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Azathoth API Server")
        }

        get("/health") {
            call.respondText("OK")
        }
    }
}
```

---

## 3. 前端实施计划 - 开发者市场模块

### 任务 3.1: 完善类型定义

**文件路径:** `website/frontend/types/index.ts`

类型定义已基本完整，无需修改。

---

### 任务 3.2: 创建 API 客户端

**文件路径:** `website/frontend/composables/useApi.ts`

```typescript
// 文件: website/frontend/composables/useApi.ts
import type { ApiResponse, Pagination } from '~/types'

export const useApi = () => {
  const config = useRuntimeConfig()
  const authStore = useAuthStore()

  const baseURL = config.public.apiBase || '/api'

  const fetchWithAuth = async <T>(
    url: string,
    options: RequestInit = {}
  ): Promise<T> => {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    }

    if (authStore.token?.accessToken) {
      headers['Authorization'] = `Bearer ${authStore.token.accessToken}`
    }

    const response = await $fetch<T>(baseURL + url, {
      ...options,
      headers,
    })

    return response
  }

  return {
    get: <T>(url: string) => fetchWithAuth<T>(url, { method: 'GET' }),
    post: <T>(url: string, body?: unknown) =>
      fetchWithAuth<T>(url, { method: 'POST', body: JSON.stringify(body) }),
    patch: <T>(url: string, body?: unknown) =>
      fetchWithAuth<T>(url, { method: 'PATCH', body: JSON.stringify(body) }),
    delete: <T>(url: string) => fetchWithAuth<T>(url, { method: 'DELETE' }),
  }
}
```

---

### 任务 3.3: 增强 Market Store

**文件路径:** `website/frontend/stores/market.ts`

```typescript
// 文件: website/frontend/stores/market.ts
import { defineStore } from 'pinia'
import type {
  MarketResource,
  ResourceType,
  LicenseType,
  Pagination,
  ResourceReview
} from '~/types'

interface MarketState {
  resources: MarketResource[]
  currentResource: MarketResource | null
  reviews: ResourceReview[]
  pagination: Pagination | null
  reviewPagination: Pagination | null
  loading: boolean
  reviewsLoading: boolean
  filters: {
    keyword: string
    type: ResourceType | null
    license: LicenseType | null
    tags: string[]
    sortBy: string
    sortOrder: 'ASC' | 'DESC'
  }
  // 用户资源管理
  userResources: MarketResource[]
  userResourcesLoading: boolean
}

export const useMarketStore = defineStore('market', {
  state: (): MarketState => ({
    resources: [],
    currentResource: null,
    reviews: [],
    pagination: null,
    reviewPagination: null,
    loading: false,
    reviewsLoading: false,
    filters: {
      keyword: '',
      type: null,
      license: null,
      tags: [],
      sortBy: 'DOWNLOADS',
      sortOrder: 'DESC',
    },
    userResources: [],
    userResourcesLoading: false,
  }),

  actions: {
    async fetchResources(page: number = 1, pageSize: number = 20) {
      this.loading = true
      try {
        const params = new URLSearchParams()
        params.set('page', page.toString())
        params.set('pageSize', pageSize.toString())

        if (this.filters.keyword) params.set('keyword', this.filters.keyword)
        if (this.filters.type) params.set('type', this.filters.type)
        if (this.filters.license) params.set('license', this.filters.license)
        if (this.filters.tags.length) params.set('tags', this.filters.tags.join(','))
        params.set('sortBy', this.filters.sortBy)
        params.set('sortOrder', this.filters.sortOrder)

        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
          pagination: Pagination
        }>(`/api/market/resources?${params}`)

        if (result.success) {
          this.resources = result.resources
          this.pagination = result.pagination
        }
      } catch (error) {
        console.error('Failed to fetch resources:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchResource(resourceId: string) {
      this.loading = true
      try {
        const result = await $fetch<{
          success: boolean
          resource: MarketResource
        }>(`/api/market/resources/${resourceId}`)

        if (result.success) {
          this.currentResource = result.resource
        }
      } catch (error) {
        console.error('Failed to fetch resource:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchResourceBySlug(slug: string) {
      this.loading = true
      try {
        const result = await $fetch<{
          success: boolean
          resource: MarketResource
        }>(`/api/market/resources/slug/${slug}`)

        if (result.success) {
          this.currentResource = result.resource
        }
      } catch (error) {
        console.error('Failed to fetch resource:', error)
        this.currentResource = null
      } finally {
        this.loading = false
      }
    },

    async fetchReviews(resourceId: string, page: number = 1, pageSize: number = 20) {
      this.reviewsLoading = true
      try {
        const result = await $fetch<{
          success: boolean
          reviews: ResourceReview[]
          pagination: Pagination
        }>(`/api/market/resources/${resourceId}/reviews?page=${page}&pageSize=${pageSize}`)

        if (result.success) {
          this.reviews = result.reviews
          this.reviewPagination = result.pagination
        }
      } catch (error) {
        console.error('Failed to fetch reviews:', error)
      } finally {
        this.reviewsLoading = false
      }
    },

    async createReview(resourceId: string, rating: number, content: string) {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) {
        throw new Error('请先登录')
      }

      const result = await $fetch<{
        success: boolean
        review?: ResourceReview
        error?: { code: string; message: string }
      }>(`/api/market/resources/${resourceId}/reviews`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${authStore.token?.accessToken}`,
          'Content-Type': 'application/json',
        },
        body: { rating, content },
      })

      if (result.success && result.review) {
        this.reviews.unshift(result.review)
        return result.review
      }

      throw new Error(result.error?.message || '创建评论失败')
    },

    async fetchPopular(type?: ResourceType, limit: number = 10) {
      try {
        const params = new URLSearchParams()
        if (type) params.set('type', type)
        params.set('limit', limit.toString())

        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
        }>(`/api/market/popular?${params}`)

        return result.success ? result.resources : []
      } catch (error) {
        console.error('Failed to fetch popular resources:', error)
        return []
      }
    },

    async fetchLatest(type?: ResourceType, limit: number = 10) {
      try {
        const params = new URLSearchParams()
        if (type) params.set('type', type)
        params.set('limit', limit.toString())

        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
        }>(`/api/market/latest?${params}`)

        return result.success ? result.resources : []
      } catch (error) {
        console.error('Failed to fetch latest resources:', error)
        return []
      }
    },

    // ========== 用户资源管理 ==========

    async fetchUserResources() {
      const authStore = useAuthStore()
      if (!authStore.user) return

      this.userResourcesLoading = true
      try {
        const result = await $fetch<{
          success: boolean
          resources: MarketResource[]
        }>(`/api/market/users/${authStore.user.userId}/resources`)

        if (result.success) {
          this.userResources = result.resources
        }
      } catch (error) {
        console.error('Failed to fetch user resources:', error)
      } finally {
        this.userResourcesLoading = false
      }
    },

    async createResource(data: {
      name: string
      description: string
      type: ResourceType
      license: LicenseType
      pricing?: { price: number; currency: string; subscriptionPeriod?: number }
      minApiVersion: string
      dependencies?: string[]
      tags?: string[]
    }) {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) {
        throw new Error('请先登录')
      }

      const result = await $fetch<{
        success: boolean
        resource?: MarketResource
        error?: { code: string; message: string }
      }>('/api/market/resources', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${authStore.token?.accessToken}`,
          'Content-Type': 'application/json',
        },
        body: data,
      })

      if (result.success && result.resource) {
        this.userResources.unshift(result.resource)
        return result.resource
      }

      throw new Error(result.error?.message || '创建资源失败')
    },

    async updateResource(resourceId: string, data: {
      name?: string
      description?: string
      pricing?: { price: number; currency: string; subscriptionPeriod?: number }
      icon?: string
      screenshots?: string[]
      tags?: string[]
    }) {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) {
        throw new Error('请先登录')
      }

      const result = await $fetch<{
        success: boolean
        resource?: MarketResource
        error?: { code: string; message: string }
      }>(`/api/market/resources/${resourceId}`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${authStore.token?.accessToken}`,
          'Content-Type': 'application/json',
        },
        body: data,
      })

      if (result.success && result.resource) {
        const index = this.userResources.findIndex(r => r.resourceId === resourceId)
        if (index !== -1) {
          this.userResources[index] = result.resource
        }
        if (this.currentResource?.resourceId === resourceId) {
          this.currentResource = result.resource
        }
        return result.resource
      }

      throw new Error(result.error?.message || '更新资源失败')
    },

    async deleteResource(resourceId: string) {
      const authStore = useAuthStore()
      if (!authStore.isAuthenticated) {
        throw new Error('请先登录')
      }

      const result = await $fetch<{
        success: boolean
        error?: { code: string; message: string }
      }>(`/api/market/resources/${resourceId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${authStore.token?.accessToken}`,
        },
      })

      if (result.success) {
        this.userResources = this.userResources.filter(r => r.resourceId !== resourceId)
        return true
      }

      throw new Error(result.error?.message || '删除资源失败')
    },

    setFilters(filters: Partial<MarketState['filters']>) {
      Object.assign(this.filters, filters)
    },

    clearFilters() {
      this.filters = {
        keyword: '',
        type: null,
        license: null,
        tags: [],
        sortBy: 'DOWNLOADS',
        sortOrder: 'DESC',
      }
    },
  },
})
```

---

### 任务 3.4: 创建资源发布页面

**文件路径:** `website/frontend/pages/market/publish.vue`

```vue
<!-- 文件: website/frontend/pages/market/publish.vue -->
<template>
  <div class="container mx-auto px-4 py-8 max-w-3xl">
    <h1 class="text-3xl font-bold mb-8 text-gray-900 dark:text-white">
      发布资源
    </h1>

    <!-- 未登录提示 -->
    <div v-if="!authStore.isAuthenticated" class="text-center py-20">
      <p class="text-gray-500 dark:text-gray-400 mb-4">请先登录后再发布资源</p>
      <NuxtLink
        to="/auth/login"
        class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
      >
        去登录
      </NuxtLink>
    </div>

    <!-- 发布表单 -->
    <form v-else @submit.prevent="handleSubmit" class="space-y-6">
      <!-- 基本信息 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">基本信息</h2>

        <div class="space-y-4">
          <!-- 资源名称 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              资源名称 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.name"
              type="text"
              required
              maxlength="100"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
              placeholder="输入资源名称"
            />
          </div>

          <!-- 资源描述 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              资源描述 <span class="text-red-500">*</span>
            </label>
            <textarea
              v-model="form.description"
              required
              rows="6"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
              placeholder="详细描述您的资源功能、使用方法等"
            ></textarea>
            <p class="text-sm text-gray-500 mt-1">支持 Markdown 格式</p>
          </div>

          <!-- 资源类型 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              资源类型 <span class="text-red-500">*</span>
            </label>
            <select
              v-model="form.type"
              required
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            >
              <option value="">请选择</option>
              <option v-for="type in resourceTypes" :key="type" :value="type">
                {{ $t(`market.types.${type}`) }}
              </option>
            </select>
          </div>

          <!-- 授权类型 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              授权类型 <span class="text-red-500">*</span>
            </label>
            <select
              v-model="form.license"
              required
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
            >
              <option value="">请选择</option>
              <option v-for="license in licenseTypes" :key="license" :value="license">
                {{ $t(`market.licenses.${license}`) }}
              </option>
            </select>
          </div>
        </div>
      </div>

      <!-- 定价信息 (付费资源) -->
      <div v-if="isPaidLicense" class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">定价信息</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              价格 (元) <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.price"
              type="number"
              min="0.01"
              step="0.01"
              required
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              placeholder="0.00"
            />
          </div>

          <div v-if="form.license === 'PAID_SUBSCRIPTION'">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              订阅周期 (天) <span class="text-red-500">*</span>
            </label>
            <input
              v-model.number="form.subscriptionPeriod"
              type="number"
              min="1"
              required
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              placeholder="30"
            />
          </div>
        </div>
      </div>

      <!-- 技术信息 -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-900 dark:text-white">技术信息</h2>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              最低 API 版本 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="form.minApiVersion"
              type="text"
              required
              pattern="\d+\.\d+\.\d+"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              placeholder="1.0.0"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              标签
            </label>
            <input
              v-model="tagsInput"
              type="text"
              class="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              placeholder="用逗号分隔多个标签，如: rpg, skill, combat"
            />
          </div>
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="flex justify-end gap-4">
        <NuxtLink
          to="/market"
          class="px-6 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700"
        >
          取消
        </NuxtLink>
        <button
          type="submit"
          :disabled="submitting"
          class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 disabled:opacity-50"
        >
          {{ submitting ? '提交中...' : '创建资源' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ResourceType, LicenseType } from '~/types'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()
const marketStore = useMarketStore()

const resourceTypes = Object.values(ResourceType)
const licenseTypes = Object.values(LicenseType)

const form = reactive({
  name: '',
  description: '',
  type: '' as ResourceType | '',
  license: '' as LicenseType | '',
  price: 0,
  subscriptionPeriod: 30,
  minApiVersion: '1.0.0',
})

const tagsInput = ref('')
const submitting = ref(false)

const isPaidLicense = computed(() =>
  form.license === 'PAID_PERPETUAL' || form.license === 'PAID_SUBSCRIPTION'
)

const handleSubmit = async () => {
  if (!form.name || !form.description || !form.type || !form.license) {
    alert('请填写所有必填项')
    return
  }

  submitting.value = true
  try {
    const tags = tagsInput.value
      .split(',')
      .map(t => t.trim())
      .filter(t => t.length > 0)

    const pricing = isPaidLicense.value
      ? {
          price: Math.round(form.price * 100), // 转换为分
          currency: 'CNY',
          subscriptionPeriod: form.license === 'PAID_SUBSCRIPTION' ? form.subscriptionPeriod : undefined,
        }
      : undefined

    const resource = await marketStore.createResource({
      name: form.name,
      description: form.description,
      type: form.type as ResourceType,
      license: form.license as LicenseType,
      pricing,
      minApiVersion: form.minApiVersion,
      tags,
    })

    alert('资源创建成功！现在您可以上传第一个版本。')
    router.push(`/market/${resource.slug}`)
  } catch (error: any) {
    alert(error.message || '创建资源失败')
  } finally {
    submitting.value = false
  }
}

useHead({
  title: '发布资源 - Azathoth Market',
})
</script>
```

---

### 任务 3.5: 创建用户资源管理页面

**文件路径:** `website/frontend/pages/market/my-resources.vue`

```vue
<!-- 文件: website/frontend/pages/market/my-resources.vue -->
<template>
  <div class="container mx-auto px-4 py-8">
    <div class="flex justify-between items-center mb-8">
      <h1 class="text-3xl font-bold text-gray-900 dark:text-white">
        我的资源
      </h1>
      <NuxtLink
        to="/market/publish"
        class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
      >
        发布新资源
      </NuxtLink>
    </div>

    <!-- 未登录 -->
    <div v-if="!authStore.isAuthenticated" class="text-center py-20">
      <p class="text-gray-500 dark:text-gray-400 mb-4">请先登录</p>
      <NuxtLink
        to="/auth/login"
        class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
      >
        去登录
      </NuxtLink>
    </div>

    <!-- 加载中 -->
    <div v-else-if="marketStore.userResourcesLoading" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto"></div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!marketStore.userResources.length" class="text-center py-20">
      <p class="text-gray-500 dark:text-gray-400 mb-4">您还没有发布任何资源</p>
      <NuxtLink
        to="/market/publish"
        class="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
      >
        发布第一个资源
      </NuxtLink>
    </div>

    <!-- 资源列表 -->
    <div v-else class="space-y-4">
      <div
        v-for="resource in marketStore.userResources"
        :key="resource.resourceId"
        class="bg-white dark:bg-gray-800 rounded-lg shadow p-6"
      >
        <div class="flex items-start gap-4">
          <img
            :src="resource.icon || '/default-resource-icon.png'"
            :alt="resource.name"
            class="w-16 h-16 rounded-lg object-cover"
          />
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
              <NuxtLink
                :to="`/market/${resource.slug}`"
                class="text-xl font-semibold text-gray-900 dark:text-white hover:text-primary-600"
              >
                {{ resource.name }}
              </NuxtLink>
              <span
                :class="statusClass(resource.status)"
                class="px-2 py-0.5 text-xs rounded"
              >
                {{ statusText(resource.status) }}
              </span>
            </div>
            <p class="text-gray-600 dark:text-gray-400 text-sm mb-2 line-clamp-2">
              {{ resource.description }}
            </p>
            <div class="flex items-center gap-4 text-sm text-gray-500">
              <span>v{{ resource.latestVersion }}</span>
              <span>{{ resource.downloads }} 下载</span>
              <span>{{ resource.rating.toFixed(1) }} 评分</span>
            </div>
          </div>
          <div class="flex gap-2">
            <NuxtLink
              :to="`/market/${resource.slug}/edit`"
              class="px-3 py-1 text-sm border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              编辑
            </NuxtLink>
            <NuxtLink
              :to="`/market/${resource.slug}/versions`"
              class="px-3 py-1 text-sm border border-gray-300 dark:border-gray-600 rounded hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              版本管理
            </NuxtLink>
            <button
              @click="handleDelete(resource.resourceId)"
              class="px-3 py-1 text-sm text-red-600 border border-red-300 rounded hover:bg-red-50"
            >
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ResourceStatus } from '~/types'

const authStore = useAuthStore()
const marketStore = useMarketStore()

onMounted(() => {
  if (authStore.isAuthenticated) {
    marketStore.fetchUserResources()
  }
})

const statusClass = (status: ResourceStatus) => {
  const classes: Record<string, string> = {
    DRAFT: 'bg-gray-100 text-gray-700',
    PENDING: 'bg-yellow-100 text-yellow-700',
    APPROVED: 'bg-green-100 text-green-700',
    REJECTED: 'bg-red-100 text-red-700',
    SUSPENDED: 'bg-red-100 text-red-700',
  }
  return classes[status] || classes.DRAFT
}

const statusText = (status: ResourceStatus) => {
  const texts: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '审核中',
    APPROVED: '已发布',
    REJECTED: '已拒绝',
    SUSPENDED: '已下架',
  }
  return texts[status] || status
}

const handleDelete = async (resourceId: string) => {
  if (!confirm('确定要删除此资源吗？此操作不可恢复。')) return

  try {
    await marketStore.deleteResource(resourceId)
    alert('删除成功')
  } catch (error: any) {
    alert(error.message || '删除失败')
  }
}

useHead({
  title: '我的资源 - Azathoth Market',
})
</script>
```

---

## 4. 前端实施计划 - Docs 文档页面

### 任务 4.1: 重构文档结构

将 wiki 目录重命名为 docs，并添加 Azathoth 框架开发者文档（不包含主站搭建内容）。

**新的文档结构:**

```
website/frontend/content/
└── docs/
    ├── index.md                    # 文档首页
    ├── getting-started/            # 快速开始
    │   ├── index.md               # 概述
    │   ├── installation.md        # 安装指南
    │   └── first-plugin.md        # 第一个插件
    ├── core-concepts/              # 核心概念
    │   ├── index.md               # 概述
    │   ├── architecture.md        # 架构设计
    │   ├── plugin-system.md       # 插件系统
    │   └── event-system.md        # 事件系统
    ├── api-reference/              # API 参考
    │   ├── index.md               # 概述
    │   ├── player-api.md          # 玩家 API
    │   ├── world-api.md           # 世界 API
    │   ├── entity-api.md          # 实体 API
    │   ├── command-api.md         # 命令 API
    │   └── event-api.md           # 事件 API
    ├── guides/                     # 开发指南
    │   ├── index.md               # 概述
    │   ├── skill-system.md        # 技能系统开发
    │   ├── dungeon-system.md      # 副本系统开发
    │   ├── npc-system.md          # NPC 系统开发
    │   └── database.md            # 数据库集成
    ├── advanced/                   # 高级主题
    │   ├── index.md               # 概述
    │   ├── performance.md         # 性能优化
    │   ├── scaling.md             # 水平扩展
    │   └── deployment.md          # 部署指南
    └── changelog.md                # 更新日志
```

---

### 任务 4.2: 创建文档首页

**文件路径:** `website/frontend/content/docs/index.md`

```markdown
---
title: Azathoth 框架文档
description: Azathoth MMORPG 级 Minecraft 服务器框架官方文档
navigation:
  title: 首页
  order: 1
---

# Azathoth 框架文档

欢迎使用 Azathoth —— 一个高性能的 MMORPG 级 Minecraft 服务器框架。

## 什么是 Azathoth？

Azathoth 是一个基于 [Minestom](https://minestom.net) 的 Minecraft 服务器框架，专为构建大型多人在线角色扮演游戏（MMORPG）而设计。它提供了：

- **微服务架构** - 水平可扩展的分布式系统
- **高性能引擎** - 基于 Minestom 的轻量级游戏引擎
- **完整的游戏系统** - 技能、副本、公会、交易等内置系统
- **插件生态** - 灵活的插件系统和开发者市场
- **Kotlin 优先** - 现代化的 Kotlin API 设计

## 快速开始

::card-grid
#default
  ::card{icon="i-heroicons-rocket-launch"}
  #title
  安装指南
  #description
  了解如何搭建开发环境和运行第一个服务器
  ::

  ::card{icon="i-heroicons-puzzle-piece"}
  #title
  第一个插件
  #description
  创建您的第一个 Azathoth 插件
  ::

  ::card{icon="i-heroicons-book-open"}
  #title
  核心概念
  #description
  理解 Azathoth 的架构和设计理念
  ::

  ::card{icon="i-heroicons-code-bracket"}
  #title
  API 参考
  #description
  完整的 API 文档和示例代码
  ::
::

## 系统要求

- **JDK 21+** (推荐 JDK 25)
- **Kotlin 2.0+**
- **Gradle 9.0+**
- **PostgreSQL 15+** (可选)
- **Redis 7+** (可选)

## 获取帮助

- [GitHub Issues](https://github.com/azathoth-mc/azathoth/issues) - 报告 Bug 或请求功能
- [Discord 社区](https://discord.gg/azathoth) - 加入开发者社区
- [论坛](https://forum.azathoth.dev) - 讨论和分享

## 贡献

Azathoth 是开源项目，欢迎贡献代码、文档和想法！

查看 [贡献指南](https://github.com/azathoth-mc/azathoth/blob/main/CONTRIBUTING.md) 了解如何参与。
```

---

### 任务 4.3: 创建快速开始文档

**文件路径:** `website/frontend/content/docs/getting-started/index.md`

```markdown
---
title: 快速开始
description: 开始使用 Azathoth 框架进行开发
navigation:
  title: 快速开始
  order: 2
---

# 快速开始

本章节将引导您完成 Azathoth 开发环境的搭建，并创建您的第一个插件。

## 前置条件

在开始之前，请确保您的开发环境满足以下要求：

### 必需软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 21+ (推荐 25) | Azathoth 使用最新的 Java 特性 |
| Kotlin | 2.0+ | 主要开发语言 |
| Gradle | 9.0+ | 构建工具 |
| Git | 最新版 | 版本控制 |

### 推荐 IDE

- **IntelliJ IDEA** (推荐) - 最佳 Kotlin 开发体验
- **VS Code** + Kotlin 扩展 - 轻量级选择

## 创建新项目

### 方式一：使用项目生成器（推荐）

访问 [项目生成器](/generator) 创建新项目：

1. 选择项目类型（插件/模块/服务）
2. 配置项目信息（名称、包名等）
3. 选择需要的功能模块
4. 下载生成的项目模板

### 方式二：手动创建

1. 创建 `build.gradle.kts`：

```kotlin
plugins {
    kotlin("jvm") version "2.3.0"
    id("com.azathoth.plugin") version "1.0.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.azathoth.dev/releases")
}

dependencies {
    compileOnly("com.azathoth:azathoth-api:1.0.0")
}

azathoth {
    pluginName = "MyPlugin"
    mainClass = "com.example.MyPlugin"
    apiVersion = "1.0.0"
}
```

2. 创建主类：

```kotlin
// src/main/kotlin/com/example/MyPlugin.kt
package com.example

import com.azathoth.sdk.plugin.AzathothPlugin
import com.azathoth.sdk.plugin.annotation.Plugin

@Plugin(
    id = "my-plugin",
    name = "My Plugin",
    version = "1.0.0",
    authors = ["Your Name"]
)
class MyPlugin : AzathothPlugin() {

    override fun onEnable() {
        logger.info("MyPlugin 已启用!")
    }

    override fun onDisable() {
        logger.info("MyPlugin 已禁用!")
    }
}
```

## 运行开发服务器

```bash
# 构建插件
./gradlew build

# 复制到测试服务器
cp build/libs/my-plugin-1.0.0.jar ../test-server/plugins/

# 启动测试服务器
cd ../test-server && java -jar azathoth-server.jar
```

## 下一步

- [安装指南](/docs/getting-started/installation) - 详细的环境配置
- [第一个插件](/docs/getting-started/first-plugin) - 完整的插件开发教程
- [核心概念](/docs/core-concepts) - 理解框架架构
```

---

### 任务 4.4: 创建 API 参考文档

**文件路径:** `website/frontend/content/docs/api-reference/player-api.md`

```markdown
---
title: 玩家 API
description: Azathoth 玩家相关 API 文档
navigation:
  title: 玩家 API
  order: 2
---

# 玩家 API

Azathoth 提供了丰富的玩家管理 API，用于处理玩家数据、状态和交互。

## GamePlayer 接口

`GamePlayer` 是 Azathoth 中玩家的核心接口，扩展了 Minestom 的 `Player` 类。

### 基本属性

```kotlin
interface GamePlayer : Player {
    /** 玩家唯一标识符 */
    val playerId: UUID

    /** 玩家数据 */
    val data: PlayerData

    /** 玩家统计 */
    val stats: PlayerStats

    /** 玩家背包 */
    val inventory: PlayerInventory

    /** 玩家技能 */
    val skills: SkillManager

    /** 玩家任务 */
    val quests: QuestManager

    /** 玩家成就 */
    val achievements: AchievementManager
}
```

### 获取玩家实例

```kotlin
// 通过 UUID 获取
val player = AzathothAPI.players.get(uuid)

// 通过名称获取
val player = AzathothAPI.players.getByName("PlayerName")

// 获取所有在线玩家
val onlinePlayers = AzathothAPI.players.getOnline()
```

## PlayerData 数据管理

### 读取数据

```kotlin
// 获取基本信息
val level = player.data.level
val exp = player.data.experience
val coins = player.data.coins

// 获取自定义数据
val customValue = player.data.get<Int>("my_custom_key")
val customObject = player.data.get<MyData>("my_data")
```

### 写入数据

```kotlin
// 设置基本属性
player.data.level = 10
player.data.experience = 5000L
player.data.coins = 1000L

// 设置自定义数据
player.data.set("my_custom_key", 42)
player.data.set("my_data", MyData(foo = "bar"))

// 保存数据（通常自动保存）
player.data.save()
```

## PlayerStats 统计系统

### 属性类型

```kotlin
enum class StatType {
    // 基础属性
    MAX_HEALTH,     // 最大生命值
    HEALTH_REGEN,   // 生命回复
    MAX_MANA,       // 最大法力值
    MANA_REGEN,     // 法力回复

    // 攻击属性
    ATTACK_DAMAGE,  // 攻击力
    ATTACK_SPEED,   // 攻击速度
    CRITICAL_RATE,  // 暴击率
    CRITICAL_DAMAGE,// 暴击伤害

    // 防御属性
    DEFENSE,        // 防御力
    DODGE_RATE,     // 闪避率
    BLOCK_RATE,     // 格挡率

    // 其他属性
    MOVEMENT_SPEED, // 移动速度
    LUCK,           // 幸运值
}
```

### 使用示例

```kotlin
// 获取属性值
val attack = player.stats.get(StatType.ATTACK_DAMAGE)
val defense = player.stats.get(StatType.DEFENSE)

// 获取基础值和加成值
val baseAttack = player.stats.getBase(StatType.ATTACK_DAMAGE)
val bonusAttack = player.stats.getBonus(StatType.ATTACK_DAMAGE)

// 添加临时加成
player.stats.addModifier(
    StatType.ATTACK_DAMAGE,
    StatModifier(
        id = "buff_attack",
        value = 50.0,
        type = ModifierType.FLAT,  // 或 PERCENT
        duration = Duration.seconds(30)
    )
)

// 移除加成
player.stats.removeModifier(StatType.ATTACK_DAMAGE, "buff_attack")
```

## 事件监听

### 玩家事件

```kotlin
@EventHandler
fun onPlayerJoin(event: PlayerJoinEvent) {
    val player = event.player as GamePlayer
    player.sendMessage("欢迎来到服务器！")
}

@EventHandler
fun onPlayerLevelUp(event: PlayerLevelUpEvent) {
    val player = event.player
    val newLevel = event.newLevel

    // 发送全服公告
    AzathothAPI.broadcast("${player.name} 升级到了 $newLevel 级！")
}

@EventHandler
fun onPlayerDeath(event: PlayerDeathEvent) {
    val player = event.player as GamePlayer
    val killer = event.killer

    // 处理死亡逻辑
    player.data.deaths++
    killer?.let {
        (it as? GamePlayer)?.data?.kills++
    }
}
```

## 完整示例

```kotlin
@Plugin(id = "player-demo")
class PlayerDemoPlugin : AzathothPlugin() {

    override fun onEnable() {
        // 注册命令
        registerCommand("stats") { player, args ->
            val gamePlayer = player as GamePlayer

            player.sendMessage("""
                === 你的属性 ===
                等级: ${gamePlayer.data.level}
                经验: ${gamePlayer.data.experience}
                攻击力: ${gamePlayer.stats.get(StatType.ATTACK_DAMAGE)}
                防御力: ${gamePlayer.stats.get(StatType.DEFENSE)}
            """.trimIndent())
        }

        // 监听事件
        eventBus.subscribe<PlayerJoinEvent> { event ->
            val player = event.player as GamePlayer

            // 检查是否为新玩家
            if (player.data.level == 0) {
                initializeNewPlayer(player)
            }

            // 加载玩家数据
            player.data.load()
        }
    }

    private fun initializeNewPlayer(player: GamePlayer) {
        player.data.level = 1
        player.data.experience = 0
        player.data.coins = 100

        player.sendMessage("欢迎新玩家！你获得了 100 金币作为新手礼物。")
    }
}
```

## 相关链接

- [实体 API](/docs/api-reference/entity-api) - 实体管理
- [技能系统](/docs/guides/skill-system) - 技能开发指南
- [事件 API](/docs/api-reference/event-api) - 事件系统详解
```

---

### 任务 4.5: 更新文档布局

**文件路径:** `website/frontend/layouts/docs.vue`

```vue
<!-- 文件: website/frontend/layouts/docs.vue -->
<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <header class="sticky top-0 z-50 bg-white dark:bg-gray-800 shadow-sm">
      <nav class="container mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <!-- Logo -->
          <NuxtLink to="/" class="flex items-center space-x-2">
            <img src="/logo.svg" alt="Azathoth" class="h-8 w-8" />
            <span class="text-xl font-bold text-gray-900 dark:text-white">Azathoth</span>
          </NuxtLink>

          <!-- Search -->
          <div class="hidden md:block flex-1 max-w-md mx-8">
            <div class="relative">
              <input
                v-model="searchQuery"
                type="text"
                placeholder="搜索文档..."
                class="w-full px-4 py-2 pl-10 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-primary-500"
                @keyup.enter="handleSearch"
              />
              <svg class="absolute left-3 top-2.5 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
            </div>
          </div>

          <!-- Desktop Navigation -->
          <div class="hidden md:flex items-center space-x-6">
            <NuxtLink
              v-for="item in navItems"
              :key="item.to"
              :to="item.to"
              class="text-gray-600 dark:text-gray-300 hover:text-primary-600 dark:hover:text-primary-400"
            >
              {{ item.label }}
            </NuxtLink>

            <!-- GitHub -->
            <a
              href="https://github.com/azathoth-mc/azathoth"
              target="_blank"
              class="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24">
                <path fill-rule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" clip-rule="evenodd" />
              </svg>
            </a>

            <!-- Theme Toggle -->
            <button
              @click="toggleDarkMode"
              class="p-2 text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg v-if="isDark" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
              </svg>
              <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
              </svg>
            </button>
          </div>

          <!-- Mobile Menu Button -->
          <button
            @click="showMobileMenu = !showMobileMenu"
            class="md:hidden p-2 text-gray-500"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="!showMobileMenu" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </nav>
    </header>

    <!-- Main Content -->
    <div class="flex">
      <!-- Sidebar -->
      <aside
        class="hidden lg:block w-64 flex-shrink-0 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700 min-h-[calc(100vh-64px)] sticky top-16 overflow-y-auto"
      >
        <nav class="p-4">
          <div v-for="section in sidebarSections" :key="section.title" class="mb-6">
            <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">
              {{ section.title }}
            </h3>
            <ul class="space-y-1">
              <li v-for="item in section.items" :key="item.to">
                <NuxtLink
                  :to="item.to"
                  class="block px-3 py-2 rounded-md text-sm transition-colors"
                  :class="isActiveRoute(item.to)
                    ? 'bg-primary-100 dark:bg-primary-900 text-primary-700 dark:text-primary-300 font-medium'
                    : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'"
                >
                  {{ item.label }}
                </NuxtLink>
              </li>
            </ul>
          </div>
        </nav>
      </aside>

      <!-- Mobile Sidebar -->
      <div
        v-if="showMobileSidebar"
        class="lg:hidden fixed inset-0 z-40 bg-black/50"
        @click="showMobileSidebar = false"
      >
        <aside class="w-64 bg-white dark:bg-gray-800 min-h-full overflow-y-auto" @click.stop>
          <nav class="p-4">
            <div class="flex justify-between items-center mb-4">
              <span class="font-semibold text-gray-900 dark:text-white">文档导航</span>
              <button @click="showMobileSidebar = false" class="text-gray-500">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <div v-for="section in sidebarSections" :key="section.title" class="mb-6">
              <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-2">
                {{ section.title }}
              </h3>
              <ul class="space-y-1">
                <li v-for="item in section.items" :key="item.to">
                  <NuxtLink
                    :to="item.to"
                    class="block px-3 py-2 rounded-md text-sm"
                    :class="isActiveRoute(item.to) ? 'bg-primary-100 text-primary-700 font-medium' : 'text-gray-700 hover:bg-gray-100'"
                    @click="showMobileSidebar = false"
                  >
                    {{ item.label }}
                  </NuxtLink>
                </li>
              </ul>
            </div>
          </nav>
        </aside>
      </div>

      <!-- Page Content -->
      <main class="flex-1 min-w-0">
        <!-- Mobile Sidebar Toggle -->
        <button
          @click="showMobileSidebar = true"
          class="lg:hidden fixed bottom-4 right-4 z-30 p-3 bg-primary-600 text-white rounded-full shadow-lg hover:bg-primary-700"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <slot />
      </main>

      <!-- Table of Contents (右侧) -->
      <aside class="hidden xl:block w-56 flex-shrink-0 sticky top-16 h-[calc(100vh-64px)] overflow-y-auto">
        <nav v-if="toc.length" class="p-4">
          <h3 class="text-sm font-semibold text-gray-500 dark:text-gray-400 uppercase tracking-wider mb-3">
            本页目录
          </h3>
          <ul class="space-y-2 text-sm">
            <li v-for="item in toc" :key="item.id">
              <a
                :href="`#${item.id}`"
                class="text-gray-600 dark:text-gray-400 hover:text-primary-600 dark:hover:text-primary-400"
                :class="{ 'pl-4': item.depth > 2 }"
              >
                {{ item.text }}
              </a>
            </li>
          </ul>
        </nav>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n()
const colorMode = useColorMode()
const route = useRoute()
const router = useRouter()

const isDark = computed(() => colorMode.value === 'dark')
const showMobileMenu = ref(false)
const showMobileSidebar = ref(false)
const searchQuery = ref('')
const toc = ref<{ id: string; text: string; depth: number }[]>([])

const navItems = [
  { to: '/market', label: '资源市场' },
  { to: '/generator', label: '项目生成器' },
  { to: '/forum', label: '论坛' },
  { to: '/docs', label: '文档' },
]

const sidebarSections = [
  {
    title: '快速开始',
    items: [
      { to: '/docs', label: '概述' },
      { to: '/docs/getting-started', label: '开始使用' },
      { to: '/docs/getting-started/installation', label: '安装指南' },
      { to: '/docs/getting-started/first-plugin', label: '第一个插件' },
    ],
  },
  {
    title: '核心概念',
    items: [
      { to: '/docs/core-concepts', label: '概述' },
      { to: '/docs/core-concepts/architecture', label: '架构设计' },
      { to: '/docs/core-concepts/plugin-system', label: '插件系统' },
      { to: '/docs/core-concepts/event-system', label: '事件系统' },
    ],
  },
  {
    title: 'API 参考',
    items: [
      { to: '/docs/api-reference', label: '概述' },
      { to: '/docs/api-reference/player-api', label: '玩家 API' },
      { to: '/docs/api-reference/world-api', label: '世界 API' },
      { to: '/docs/api-reference/entity-api', label: '实体 API' },
      { to: '/docs/api-reference/command-api', label: '命令 API' },
      { to: '/docs/api-reference/event-api', label: '事件 API' },
    ],
  },
  {
    title: '开发指南',
    items: [
      { to: '/docs/guides', label: '概述' },
      { to: '/docs/guides/skill-system', label: '技能系统' },
      { to: '/docs/guides/dungeon-system', label: '副本系统' },
      { to: '/docs/guides/npc-system', label: 'NPC 系统' },
      { to: '/docs/guides/database', label: '数据库集成' },
    ],
  },
  {
    title: '高级主题',
    items: [
      { to: '/docs/advanced', label: '概述' },
      { to: '/docs/advanced/performance', label: '性能优化' },
      { to: '/docs/advanced/scaling', label: '水平扩展' },
      { to: '/docs/advanced/deployment', label: '部署指南' },
    ],
  },
]

const toggleDarkMode = () => {
  colorMode.preference = isDark.value ? 'light' : 'dark'
}

const isActiveRoute = (path: string) => {
  const currentPath = route.path.replace(/\/$/, '')
  const targetPath = path.replace(/\/$/, '')
  return currentPath === targetPath
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    router.push(`/docs/search?q=${encodeURIComponent(searchQuery.value)}`)
  }
}

// 提取页面目录
onMounted(() => {
  const headings = document.querySelectorAll('.prose h2, .prose h3')
  toc.value = Array.from(headings).map(el => ({
    id: el.id,
    text: el.textContent || '',
    depth: parseInt(el.tagName.charAt(1)),
  }))
})
</script>
```

---

### 任务 4.6: 更新文档内容页面

**文件路径:** `website/frontend/pages/docs/[...slug].vue`

```vue
<!-- 文件: website/frontend/pages/docs/[...slug].vue -->
<template>
  <div class="p-6 lg:p-8 max-w-4xl">
    <ContentDoc v-slot="{ doc }">
      <article class="prose dark:prose-invert max-w-none prose-headings:scroll-mt-20">
        <!-- Breadcrumb -->
        <nav class="text-sm mb-6 not-prose">
          <ol class="flex items-center space-x-2 text-gray-500 dark:text-gray-400">
            <li>
              <NuxtLink to="/docs" class="hover:text-primary-600">
                文档
              </NuxtLink>
            </li>
            <template v-if="breadcrumbs.length">
              <li v-for="(crumb, index) in breadcrumbs" :key="index">
                <span class="mx-2">/</span>
                <NuxtLink v-if="crumb.to" :to="crumb.to" class="hover:text-primary-600">
                  {{ crumb.label }}
                </NuxtLink>
                <span v-else class="text-gray-900 dark:text-white">{{ crumb.label }}</span>
              </li>
            </template>
          </ol>
        </nav>

        <!-- Title -->
        <h1 class="text-3xl font-bold text-gray-900 dark:text-white mb-2">
          {{ doc.title }}
        </h1>
        <p v-if="doc.description" class="text-lg text-gray-600 dark:text-gray-400 mb-8">
          {{ doc.description }}
        </p>

        <!-- Content -->
        <ContentRenderer :value="doc" />

        <!-- Footer Navigation -->
        <div class="mt-12 pt-8 border-t border-gray-200 dark:border-gray-700 not-prose">
          <div class="flex justify-between">
            <ContentPrevNext
              class="flex justify-between w-full"
            >
              <template #prev="{ prev }">
                <NuxtLink
                  v-if="prev"
                  :to="prev._path"
                  class="flex items-center space-x-2 text-primary-600 hover:underline"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                  </svg>
                  <span>{{ prev.title }}</span>
                </NuxtLink>
              </template>
              <template #next="{ next }">
                <NuxtLink
                  v-if="next"
                  :to="next._path"
                  class="flex items-center space-x-2 text-primary-600 hover:underline ml-auto"
                >
                  <span>{{ next.title }}</span>
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                  </svg>
                </NuxtLink>
              </template>
            </ContentPrevNext>
          </div>
        </div>

        <!-- Edit on GitHub -->
        <div class="mt-8 text-sm text-gray-500 dark:text-gray-400 not-prose">
          <a
            :href="`https://github.com/azathoth-mc/azathoth/edit/main/website/frontend/content${route.path}.md`"
            target="_blank"
            class="inline-flex items-center hover:text-primary-600"
          >
            <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            在 GitHub 上编辑此页
          </a>
        </div>
      </article>
    </ContentDoc>
  </div>
</template>

<script setup lang="ts">
definePageMeta({
  layout: 'docs'
})

const route = useRoute()

// 计算面包屑
const breadcrumbs = computed(() => {
  const parts = route.path.split('/').filter(p => p && p !== 'docs')
  const crumbs: { label: string; to?: string }[] = []

  let path = '/docs'
  for (let i = 0; i < parts.length; i++) {
    path += '/' + parts[i]
    const isLast = i === parts.length - 1
    crumbs.push({
      label: formatLabel(parts[i]),
      to: isLast ? undefined : path,
    })
  }

  return crumbs
})

const formatLabel = (slug: string) => {
  return slug
    .split('-')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

// SEO
const { data: doc } = await useAsyncData(`content-${route.path}`, () => {
  return queryContent(route.path).findOne()
})

useHead({
  title: doc.value?.title ? `${doc.value.title} - Azathoth Docs` : 'Azathoth Docs',
  meta: [
    { name: 'description', content: doc.value?.description || 'Azathoth 框架文档' }
  ]
})
</script>
```

---

## 5. 验证步骤

### 5.1 后端验证

```bash
# 1. 编译后端代码
cd website/backend
../../gradlew compileKotlin

# 2. 运行单元测试
../../gradlew test

# 3. 启动开发服务器
../../gradlew run

# 4. 测试 API 端点
# 搜索资源
curl http://localhost:8080/api/market/resources

# 获取热门资源
curl http://localhost:8080/api/market/popular

# 健康检查
curl http://localhost:8080/health
```

### 5.2 前端验证

```bash
# 1. 安装依赖
cd website/frontend
pnpm install

# 2. 类型检查
pnpm typecheck

# 3. 启动开发服务器
pnpm dev

# 4. 访问页面验证
# - 市场列表: http://localhost:3000/market
# - 资源详情: http://localhost:3000/market/{slug}
# - 发布资源: http://localhost:3000/market/publish
# - 我的资源: http://localhost:3000/market/my-resources
# - 文档首页: http://localhost:3000/docs
# - API 文档: http://localhost:3000/docs/api-reference/player-api
```

### 5.3 集成测试清单

- [ ] 市场搜索功能正常工作
- [ ] 资源详情页正确显示
- [ ] 用户登录后可以发布资源
- [ ] 用户可以管理自己的资源
- [ ] 评论功能正常工作
- [ ] 文档页面正确渲染 Markdown
- [ ] 文档导航功能正常
- [ ] 文档搜索功能正常
- [ ] 响应式布局在移动端正常显示
- [ ] 暗色模式正常工作

---

## 附录: 文件清单

### 后端新增文件

| 文件路径 | 说明 |
|---------|------|
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/table/MarketTables.kt` | 数据库表定义 |
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/dto/MarketDTOs.kt` | 数据传输对象 |
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/dto/MarketRequests.kt` | 请求对象 |
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/dto/MarketResponses.kt` | 响应对象 |
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/repository/MarketRepository.kt` | 市场仓储 |
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/repository/ReviewRepository.kt` | 评论仓储 |
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/impl/MarketServiceImpl.kt` | 服务实现 |
| `website/backend/src/main/kotlin/com/azathoth/website/module/market/routes/MarketRoutes.kt` | HTTP 路由 |

### 前端新增/修改文件

| 文件路径 | 说明 |
|---------|------|
| `website/frontend/composables/useApi.ts` | API 客户端 |
| `website/frontend/stores/market.ts` | 增强后的市场 Store |
| `website/frontend/pages/market/publish.vue` | 资源发布页 |
| `website/frontend/pages/market/my-resources.vue` | 我的资源页 |
| `website/frontend/layouts/docs.vue` | 文档布局 |
| `website/frontend/pages/docs/[...slug].vue` | 文档内容页 |
| `website/frontend/content/docs/*.md` | 文档内容文件 |

---

**计划完成。** 按照此计划依次实施各任务，每完成一个任务后进行验证，确保功能正常后再进行下一步。
