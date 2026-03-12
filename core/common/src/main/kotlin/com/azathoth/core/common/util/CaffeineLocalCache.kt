package com.azathoth.core.common.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache as CaffeineCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.toJavaDuration

/**
 * 基于 Caffeine 的本地缓存实现
 */
class CaffeineLocalCache<K : Any, V : Any>(
    maximumSize: Long = 10_000,
    defaultTtl: Duration? = null,
    recordStats: Boolean = false
) : StatsCache<K, V> {

    private val cache: CaffeineCache<K, CacheEntry<V>>
    private val mutex = Mutex()

    init {
        val builder = Caffeine.newBuilder().maximumSize(maximumSize)
        if (defaultTtl != null) {
            builder.expireAfterWrite(defaultTtl.toJavaDuration())
        }
        if (recordStats) {
            builder.recordStats()
        }
        cache = builder.build()
    }

    private data class CacheEntry<V>(val value: V, val expiresAt: Long? = null) {
        fun isExpired(): Boolean = expiresAt != null && System.currentTimeMillis() > expiresAt
    }

    override suspend fun get(key: K): V? {
        val entry = cache.getIfPresent(key) ?: return null
        if (entry.isExpired()) {
            cache.invalidate(key)
            return null
        }
        return entry.value
    }

    override suspend fun get(key: K, loader: suspend (K) -> V): V {
        val existing = get(key)
        if (existing != null) return existing
        return mutex.withLock {
            // 双重检查
            get(key) ?: loader(key).also { put(key, it) }
        }
    }

    override suspend fun put(key: K, value: V) {
        cache.put(key, CacheEntry(value))
    }

    override suspend fun put(key: K, value: V, ttl: Duration) {
        val expiresAt = System.currentTimeMillis() + ttl.inWholeMilliseconds
        cache.put(key, CacheEntry(value, expiresAt))
    }

    override suspend fun remove(key: K): V? {
        val entry = cache.getIfPresent(key)
        cache.invalidate(key)
        return entry?.value
    }

    override suspend fun contains(key: K): Boolean = get(key) != null

    override suspend fun clear() = cache.invalidateAll()

    override suspend fun size(): Long = cache.estimatedSize()

    override suspend fun keys(): Set<K> = cache.asMap().keys.toSet()

    override fun stats(): CacheStats {
        val s = cache.stats()
        return CaffeineCacheStats(
            hitCount = s.hitCount(),
            missCount = s.missCount(),
            hitRate = s.hitRate(),
            evictionCount = s.evictionCount(),
            loadCount = s.loadCount(),
            averageLoadPenalty = s.averageLoadPenalty().toLong()
        )
    }
}

private data class CaffeineCacheStats(
    override val hitCount: Long,
    override val missCount: Long,
    override val hitRate: Double,
    override val evictionCount: Long,
    override val loadCount: Long,
    override val averageLoadPenalty: Long
) : CacheStats
