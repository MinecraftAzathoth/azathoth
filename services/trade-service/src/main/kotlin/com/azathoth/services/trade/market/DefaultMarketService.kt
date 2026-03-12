package com.azathoth.services.trade.market

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.ErrorCodes
import com.azathoth.core.common.result.Result
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.min

private val logger = KotlinLogging.logger {}

// region Data Classes

data class SimpleMarketListing(
    override val listingId: String,
    override val sellerId: PlayerId,
    override val sellerName: String,
    override val itemId: String,
    override val amount: Int,
    override val unitPrice: Long,
    override val currencyType: CurrencyType,
    override val listedAt: Long = System.currentTimeMillis(),
    override val expiresAt: Long = listedAt + 7 * 24 * 3600 * 1000L, // 7天过期
    override val itemData: Map<String, Any> = emptyMap()
) : MarketListing {
    override val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
}

data class SimpleMarketSearchCriteria(
    override val itemId: String? = null,
    override val itemType: String? = null,
    override val minPrice: Long? = null,
    override val maxPrice: Long? = null,
    override val currencyType: CurrencyType? = null,
    override val sellerId: PlayerId? = null,
    override val sortBy: MarketSortBy = MarketSortBy.LISTED_TIME,
    override val sortOrder: SortOrder = SortOrder.DESC,
    override val page: Int = 1,
    override val pageSize: Int = 20
) : MarketSearchCriteria

data class SimpleMarketSearchResult(
    override val listings: List<MarketListing>,
    override val totalCount: Long,
    override val currentPage: Int,
    override val totalPages: Int
) : MarketSearchResult

data class SimplePurchaseResult(
    override val success: Boolean,
    override val listing: MarketListing? = null,
    override val purchasedAmount: Int = 0,
    override val totalCost: Long = 0,
    override val error: String? = null
) : PurchaseResult

data class SimpleTransactionRecord(
    override val recordId: String,
    override val listingId: String,
    override val sellerId: PlayerId,
    override val buyerId: PlayerId,
    override val itemId: String,
    override val amount: Int,
    override val unitPrice: Long,
    override val totalPrice: Long,
    override val currencyType: CurrencyType,
    override val transactedAt: Long = System.currentTimeMillis()
) : TransactionRecord

data class SimplePricePoint(
    override val timestamp: Long,
    override val averagePrice: Long,
    override val minPrice: Long,
    override val maxPrice: Long,
    override val volume: Long
) : PricePoint

// endregion

class DefaultMarketService : MarketService {

    private val listings = ConcurrentHashMap<String, SimpleMarketListing>()
    private val transactions = ConcurrentHashMap<String, SimpleTransactionRecord>()

    override suspend fun createListing(
        sellerId: PlayerId,
        itemId: String,
        amount: Int,
        unitPrice: Long,
        currencyType: CurrencyType,
        itemData: Map<String, Any>
    ): Result<MarketListing> {
        if (amount <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "数量必须为正数")
        if (unitPrice <= 0) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "单价必须为正数")
        if (itemId.isBlank()) return Result.failure(ErrorCodes.INVALID_ARGUMENT, "物品ID不能为空")

        val listing = SimpleMarketListing(
            listingId = UUID.randomUUID().toString(),
            sellerId = sellerId,
            sellerName = sellerId.value, // 简化：用ID作为名称
            itemId = itemId,
            amount = amount,
            unitPrice = unitPrice,
            currencyType = currencyType,
            itemData = itemData
        )
        listings[listing.listingId] = listing

        logger.info { "商品上架: ${listing.listingId} by $sellerId, $itemId x$amount @ $unitPrice" }
        return Result.success(listing)
    }

    override suspend fun cancelListing(listingId: String, operatorId: PlayerId): Result<Unit> {
        val listing = listings[listingId] ?: return Result.failure(ErrorCodes.NOT_FOUND, "商品不存在")
        if (listing.sellerId != operatorId) {
            return Result.failure(ErrorCodes.PERMISSION_DENIED, "只有卖家可以下架商品")
        }
        listings.remove(listingId)
        logger.info { "商品下架: $listingId" }
        return Result.success(Unit)
    }

    override suspend fun purchase(listingId: String, buyerId: PlayerId, amount: Int): Result<PurchaseResult> {
        val listing = listings[listingId]
            ?: return Result.success(SimplePurchaseResult(success = false, error = "商品不存在"))
        if (listing.isExpired) {
            listings.remove(listingId)
            return Result.success(SimplePurchaseResult(success = false, error = "商品已过期"))
        }
        if (listing.sellerId == buyerId) {
            return Result.success(SimplePurchaseResult(success = false, error = "不能购买自己的商品"))
        }

        val purchaseAmount = if (amount <= 0) listing.amount else min(amount, listing.amount)
        val totalCost = purchaseAmount * listing.unitPrice

        if (purchaseAmount == listing.amount) {
            listings.remove(listingId)
        } else {
            listings[listingId] = listing.copy(amount = listing.amount - purchaseAmount)
        }

        val record = SimpleTransactionRecord(
            recordId = UUID.randomUUID().toString(),
            listingId = listingId,
            sellerId = listing.sellerId,
            buyerId = buyerId,
            itemId = listing.itemId,
            amount = purchaseAmount,
            unitPrice = listing.unitPrice,
            totalPrice = totalCost,
            currencyType = listing.currencyType
        )
        transactions[record.recordId] = record

        logger.info { "交易完成: $buyerId 购买 ${listing.itemId} x$purchaseAmount, 花费 $totalCost" }
        return Result.success(
            SimplePurchaseResult(
                success = true,
                listing = listing,
                purchasedAmount = purchaseAmount,
                totalCost = totalCost
            )
        )
    }

    override suspend fun search(criteria: MarketSearchCriteria): MarketSearchResult {
        var filtered = listings.values.asSequence()
            .filter { !it.isExpired }

        criteria.itemId?.let { id -> filtered = filtered.filter { it.itemId == id } }
        criteria.itemType?.let { type -> filtered = filtered.filter { it.itemData["type"] == type } }
        criteria.minPrice?.let { min -> filtered = filtered.filter { it.unitPrice >= min } }
        criteria.maxPrice?.let { max -> filtered = filtered.filter { it.unitPrice <= max } }
        criteria.currencyType?.let { ct -> filtered = filtered.filter { it.currencyType == ct } }
        criteria.sellerId?.let { sid -> filtered = filtered.filter { it.sellerId == sid } }

        val sorted = when (criteria.sortBy) {
            MarketSortBy.PRICE -> if (criteria.sortOrder == SortOrder.ASC) filtered.sortedBy { it.unitPrice } else filtered.sortedByDescending { it.unitPrice }
            MarketSortBy.LISTED_TIME -> if (criteria.sortOrder == SortOrder.ASC) filtered.sortedBy { it.listedAt } else filtered.sortedByDescending { it.listedAt }
            MarketSortBy.AMOUNT -> if (criteria.sortOrder == SortOrder.ASC) filtered.sortedBy { it.amount } else filtered.sortedByDescending { it.amount }
        }

        val allResults = sorted.toList()
        val totalCount = allResults.size.toLong()
        val totalPages = if (totalCount == 0L) 0 else ceil(totalCount.toDouble() / criteria.pageSize).toInt()
        val offset = (criteria.page - 1) * criteria.pageSize
        val pageResults = allResults.drop(offset).take(criteria.pageSize)

        return SimpleMarketSearchResult(
            listings = pageResults,
            totalCount = totalCount,
            currentPage = criteria.page,
            totalPages = totalPages
        )
    }

    override suspend fun getListing(listingId: String): MarketListing? = listings[listingId]

    override suspend fun getPlayerListings(playerId: PlayerId): List<MarketListing> {
        return listings.values.filter { it.sellerId == playerId }
    }

    override suspend fun getPurchaseHistory(playerId: PlayerId, limit: Int): List<TransactionRecord> {
        return transactions.values
            .filter { it.buyerId == playerId }
            .sortedByDescending { it.transactedAt }
            .take(limit)
    }

    override suspend fun getSalesHistory(playerId: PlayerId, limit: Int): List<TransactionRecord> {
        return transactions.values
            .filter { it.sellerId == playerId }
            .sortedByDescending { it.transactedAt }
            .take(limit)
    }

    override suspend fun cleanupExpiredListings() {
        val removed = listings.entries.removeIf { it.value.isExpired }
        logger.info { "清理过期商品完成" }
    }

    override suspend fun getPriceHistory(itemId: String, days: Int): List<PricePoint> {
        val cutoff = System.currentTimeMillis() - days * 24 * 3600 * 1000L
        val relevant = transactions.values
            .filter { it.itemId == itemId && it.transactedAt >= cutoff }
            .groupBy { it.transactedAt / (24 * 3600 * 1000L) } // 按天分组

        return relevant.map { (dayKey, records) ->
            SimplePricePoint(
                timestamp = dayKey * 24 * 3600 * 1000L,
                averagePrice = records.map { it.unitPrice }.average().toLong(),
                minPrice = records.minOf { it.unitPrice },
                maxPrice = records.maxOf { it.unitPrice },
                volume = records.sumOf { it.amount.toLong() }
            )
        }.sortedBy { it.timestamp }
    }
}
