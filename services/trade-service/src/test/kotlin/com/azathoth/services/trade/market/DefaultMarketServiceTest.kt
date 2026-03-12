package com.azathoth.services.trade.market

import com.azathoth.core.common.identity.PlayerId
import com.azathoth.core.common.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DefaultMarketServiceTest {

    private lateinit var service: DefaultMarketService

    private val seller = PlayerId("seller-1")
    private val buyer = PlayerId("buyer-1")

    @BeforeEach
    fun setup() {
        service = DefaultMarketService()
    }

    @Test
    fun `createListing should succeed`() = runTest {
        val result = service.createListing(seller, "diamond_sword", 1, 100, CurrencyType.GOLD)
        assertTrue(result.isSuccess)
        val listing = (result as Result.Success).value
        assertEquals("diamond_sword", listing.itemId)
        assertEquals(1, listing.amount)
        assertEquals(100L, listing.unitPrice)
        assertEquals(seller, listing.sellerId)
    }

    @Test
    fun `createListing should fail with invalid amount`() = runTest {
        val result = service.createListing(seller, "diamond_sword", 0, 100, CurrencyType.GOLD)
        assertTrue(result.isFailure)
    }

    @Test
    fun `createListing should fail with invalid price`() = runTest {
        val result = service.createListing(seller, "diamond_sword", 1, -1, CurrencyType.GOLD)
        assertTrue(result.isFailure)
    }

    @Test
    fun `purchase should succeed`() = runTest {
        val listing = (service.createListing(seller, "diamond_sword", 5, 100, CurrencyType.GOLD) as Result.Success).value

        val result = service.purchase(listing.listingId, buyer, 3)
        assertTrue(result.isSuccess)
        val purchase = (result as Result.Success).value
        assertTrue(purchase.success)
        assertEquals(3, purchase.purchasedAmount)
        assertEquals(300L, purchase.totalCost)

        // 剩余2个
        val remaining = service.getListing(listing.listingId)
        assertNotNull(remaining)
        assertEquals(2, remaining!!.amount)
    }

    @Test
    fun `purchase all should remove listing`() = runTest {
        val listing = (service.createListing(seller, "diamond_sword", 5, 100, CurrencyType.GOLD) as Result.Success).value

        val result = service.purchase(listing.listingId, buyer, 0) // 0 = 全部
        assertTrue(result.isSuccess)
        val purchase = (result as Result.Success).value
        assertTrue(purchase.success)
        assertEquals(5, purchase.purchasedAmount)

        assertNull(service.getListing(listing.listingId))
    }

    @Test
    fun `purchase own listing should fail`() = runTest {
        val listing = (service.createListing(seller, "diamond_sword", 1, 100, CurrencyType.GOLD) as Result.Success).value

        val result = service.purchase(listing.listingId, seller)
        assertTrue(result.isSuccess)
        val purchase = (result as Result.Success).value
        assertFalse(purchase.success)
        assertNotNull(purchase.error)
    }

    @Test
    fun `cancelListing should work for seller`() = runTest {
        val listing = (service.createListing(seller, "diamond_sword", 1, 100, CurrencyType.GOLD) as Result.Success).value

        val result = service.cancelListing(listing.listingId, seller)
        assertTrue(result.isSuccess)
        assertNull(service.getListing(listing.listingId))
    }

    @Test
    fun `cancelListing should fail for non-seller`() = runTest {
        val listing = (service.createListing(seller, "diamond_sword", 1, 100, CurrencyType.GOLD) as Result.Success).value

        val result = service.cancelListing(listing.listingId, buyer)
        assertTrue(result.isFailure)
    }

    @Test
    fun `search should filter by itemId`() = runTest {
        service.createListing(seller, "diamond_sword", 1, 100, CurrencyType.GOLD)
        service.createListing(seller, "iron_sword", 1, 50, CurrencyType.GOLD)
        service.createListing(seller, "diamond_sword", 2, 90, CurrencyType.GOLD)

        val result = service.search(SimpleMarketSearchCriteria(itemId = "diamond_sword"))
        assertEquals(2, result.totalCount)
        assertTrue(result.listings.all { it.itemId == "diamond_sword" })
    }

    @Test
    fun `search should filter by price range`() = runTest {
        service.createListing(seller, "item1", 1, 50, CurrencyType.GOLD)
        service.createListing(seller, "item2", 1, 100, CurrencyType.GOLD)
        service.createListing(seller, "item3", 1, 200, CurrencyType.GOLD)

        val result = service.search(SimpleMarketSearchCriteria(minPrice = 60, maxPrice = 150))
        assertEquals(1, result.totalCount)
        assertEquals(100L, result.listings[0].unitPrice)
    }

    @Test
    fun `search should sort by price ascending`() = runTest {
        service.createListing(seller, "item", 1, 200, CurrencyType.GOLD)
        service.createListing(seller, "item", 1, 50, CurrencyType.GOLD)
        service.createListing(seller, "item", 1, 100, CurrencyType.GOLD)

        val result = service.search(SimpleMarketSearchCriteria(sortBy = MarketSortBy.PRICE, sortOrder = SortOrder.ASC))
        assertEquals(3, result.totalCount)
        assertEquals(50L, result.listings[0].unitPrice)
        assertEquals(100L, result.listings[1].unitPrice)
        assertEquals(200L, result.listings[2].unitPrice)
    }

    @Test
    fun `search should paginate`() = runTest {
        repeat(25) {
            service.createListing(seller, "item$it", 1, it.toLong() + 1, CurrencyType.GOLD)
        }

        val page1 = service.search(SimpleMarketSearchCriteria(page = 1, pageSize = 10))
        assertEquals(25L, page1.totalCount)
        assertEquals(10, page1.listings.size)
        assertEquals(3, page1.totalPages)

        val page3 = service.search(SimpleMarketSearchCriteria(page = 3, pageSize = 10))
        assertEquals(5, page3.listings.size)
    }

    @Test
    fun `purchase history should be recorded`() = runTest {
        val listing = (service.createListing(seller, "diamond_sword", 5, 100, CurrencyType.GOLD) as Result.Success).value
        service.purchase(listing.listingId, buyer, 3)

        val history = service.getPurchaseHistory(buyer)
        assertEquals(1, history.size)
        assertEquals(3, history[0].amount)
        assertEquals(buyer, history[0].buyerId)

        val salesHistory = service.getSalesHistory(seller)
        assertEquals(1, salesHistory.size)
        assertEquals(seller, salesHistory[0].sellerId)
    }

    @Test
    fun `getPlayerListings should return seller listings`() = runTest {
        service.createListing(seller, "item1", 1, 100, CurrencyType.GOLD)
        service.createListing(seller, "item2", 1, 200, CurrencyType.GOLD)
        service.createListing(buyer, "item3", 1, 300, CurrencyType.GOLD)

        val sellerListings = service.getPlayerListings(seller)
        assertEquals(2, sellerListings.size)
    }
}
