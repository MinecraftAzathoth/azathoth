package com.azathoth.website.module.market

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
 * 搜索结果
 */
interface MarketSearchResult {
    val resources: List<MarketResource>
    val totalCount: Long
    val page: Int
    val totalPages: Int
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
 * 下载统计
 */
interface DownloadStats {
    val totalDownloads: Long
    val lastMonthDownloads: Long
    val lastWeekDownloads: Long
    val versionDownloads: Map<String, Long>
}
