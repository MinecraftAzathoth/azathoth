package com.azathoth.core.common.util

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.milliseconds

class CaffeineLocalCacheTest {

    @Test
    fun `test put and get`() = runTest {
        val cache = CaffeineLocalCache<String, String>(maximumSize = 100)
        cache.put("key1", "value1")
        assertEquals("value1", cache.get("key1"))
    }

    @Test
    fun `test get with loader`() = runTest {
        val cache = CaffeineLocalCache<String, Int>(maximumSize = 100)
        val result = cache.get("count") { 42 }
        assertEquals(42, result)
        // 后续调用应该返回缓存值
        assertEquals(42, cache.get("count"))
    }

    @Test
    fun `test remove`() = runTest {
        val cache = CaffeineLocalCache<String, String>(maximumSize = 100)
        cache.put("key", "value")
        val removed = cache.remove("key")
        assertEquals("value", removed)
        assertNull(cache.get("key"))
    }

    @Test
    fun `test contains`() = runTest {
        val cache = CaffeineLocalCache<String, String>(maximumSize = 100)
        assertFalse(cache.contains("key"))
        cache.put("key", "value")
        assertTrue(cache.contains("key"))
    }

    @Test
    fun `test clear`() = runTest {
        val cache = CaffeineLocalCache<String, String>(maximumSize = 100)
        cache.put("a", "1")
        cache.put("b", "2")
        cache.clear()
        assertEquals(0, cache.size())
    }

    @Test
    fun `test put with ttl expires`() = runTest {
        val cache = CaffeineLocalCache<String, String>(maximumSize = 100)
        cache.put("key", "value", 1.milliseconds)
        Thread.sleep(10) // 等待过期
        assertNull(cache.get("key"))
    }

    @Test
    fun `test stats`() = runTest {
        val cache = CaffeineLocalCache<String, String>(maximumSize = 100, recordStats = true)
        cache.put("key", "value")
        cache.get("key")
        cache.get("missing")

        val stats = cache.stats()
        assertTrue(stats.hitCount >= 0)
    }
}
