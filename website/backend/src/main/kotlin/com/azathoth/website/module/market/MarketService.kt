package com.azathoth.website.module.market

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
