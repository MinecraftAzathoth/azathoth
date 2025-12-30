package com.azathoth.website.module.market.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.timestamp

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
