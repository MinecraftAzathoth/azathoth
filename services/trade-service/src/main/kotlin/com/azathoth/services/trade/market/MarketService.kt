package com.azathoth.services.trade.market

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result

/**
 * 市场商品
 */
interface MarketListing {
    /** 商品ID */
    val listingId: String
    
    /** 卖家ID */
    val sellerId: PlayerId
    
    /** 卖家名称 */
    val sellerName: String
    
    /** 物品ID */
    val itemId: String
    
    /** 物品数量 */
    val amount: Int
    
    /** 单价 */
    val unitPrice: Long
    
    /** 总价 */
    val totalPrice: Long get() = unitPrice * amount
    
    /** 货币类型 */
    val currencyType: CurrencyType
    
    /** 上架时间 */
    val listedAt: Long
    
    /** 过期时间 */
    val expiresAt: Long
    
    /** 是否已过期 */
    val isExpired: Boolean
    
    /** 物品数据 */
    val itemData: Map<String, Any>
}

/**
 * 货币类型
 */
enum class CurrencyType {
    GOLD,
    DIAMOND,
    GUILD_COIN,
    EVENT_COIN
}

/**
 * 市场搜索条件
 */
interface MarketSearchCriteria {
    /** 物品ID（可选） */
    val itemId: String?
    
    /** 物品类型（可选） */
    val itemType: String?
    
    /** 最低价格 */
    val minPrice: Long?
    
    /** 最高价格 */
    val maxPrice: Long?
    
    /** 货币类型 */
    val currencyType: CurrencyType?
    
    /** 卖家ID */
    val sellerId: PlayerId?
    
    /** 排序方式 */
    val sortBy: MarketSortBy
    
    /** 排序方向 */
    val sortOrder: SortOrder
    
    /** 页码 */
    val page: Int
    
    /** 每页数量 */
    val pageSize: Int
}

/**
 * 市场排序方式
 */
enum class MarketSortBy {
    PRICE,
    LISTED_TIME,
    AMOUNT
}

/**
 * 排序方向
 */
enum class SortOrder {
    ASC,
    DESC
}

/**
 * 搜索结果
 */
interface MarketSearchResult {
    /** 商品列表 */
    val listings: List<MarketListing>
    
    /** 总数 */
    val totalCount: Long
    
    /** 当前页 */
    val currentPage: Int
    
    /** 总页数 */
    val totalPages: Int
}

/**
 * 购买结果
 */
interface PurchaseResult {
    /** 是否成功 */
    val success: Boolean
    
    /** 购买的商品 */
    val listing: MarketListing?
    
    /** 实际购买数量 */
    val purchasedAmount: Int
    
    /** 实际花费 */
    val totalCost: Long
    
    /** 错误信息 */
    val error: String?
}

/**
 * 市场服务
 */
interface MarketService {
    /**
     * 上架商品
     */
    suspend fun createListing(
        sellerId: PlayerId,
        itemId: String,
        amount: Int,
        unitPrice: Long,
        currencyType: CurrencyType,
        itemData: Map<String, Any> = emptyMap()
    ): Result<MarketListing>
    
    /**
     * 下架商品
     */
    suspend fun cancelListing(
        listingId: String,
        operatorId: PlayerId
    ): Result<Unit>
    
    /**
     * 购买商品
     */
    suspend fun purchase(
        listingId: String,
        buyerId: PlayerId,
        amount: Int = 0 // 0 表示购买全部
    ): Result<PurchaseResult>
    
    /**
     * 搜索商品
     */
    suspend fun search(criteria: MarketSearchCriteria): MarketSearchResult
    
    /**
     * 获取商品详情
     */
    suspend fun getListing(listingId: String): MarketListing?
    
    /**
     * 获取玩家的上架商品
     */
    suspend fun getPlayerListings(playerId: PlayerId): List<MarketListing>
    
    /**
     * 获取玩家的购买历史
     */
    suspend fun getPurchaseHistory(
        playerId: PlayerId,
        limit: Int = 50
    ): List<TransactionRecord>
    
    /**
     * 获取玩家的销售历史
     */
    suspend fun getSalesHistory(
        playerId: PlayerId,
        limit: Int = 50
    ): List<TransactionRecord>
    
    /**
     * 清理过期商品
     */
    suspend fun cleanupExpiredListings()
    
    /**
     * 获取物品价格历史
     */
    suspend fun getPriceHistory(
        itemId: String,
        days: Int = 30
    ): List<PricePoint>
}

/**
 * 交易记录
 */
interface TransactionRecord {
    /** 记录ID */
    val recordId: String
    
    /** 商品ID */
    val listingId: String
    
    /** 卖家ID */
    val sellerId: PlayerId
    
    /** 买家ID */
    val buyerId: PlayerId
    
    /** 物品ID */
    val itemId: String
    
    /** 数量 */
    val amount: Int
    
    /** 单价 */
    val unitPrice: Long
    
    /** 总价 */
    val totalPrice: Long
    
    /** 货币类型 */
    val currencyType: CurrencyType
    
    /** 交易时间 */
    val transactedAt: Long
}

/**
 * 价格点
 */
interface PricePoint {
    /** 时间 */
    val timestamp: Long
    
    /** 平均价格 */
    val averagePrice: Long
    
    /** 最低价格 */
    val minPrice: Long
    
    /** 最高价格 */
    val maxPrice: Long
    
    /** 交易量 */
    val volume: Long
}
