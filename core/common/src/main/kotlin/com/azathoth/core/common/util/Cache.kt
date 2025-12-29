package com.azathoth.core.common.util

import kotlin.time.Duration

/**
 * 缓存接口
 */
interface Cache<K, V> {
    /**
     * 获取缓存值
     */
    suspend fun get(key: K): V?
    
    /**
     * 获取缓存值，不存在时使用加载器
     */
    suspend fun get(key: K, loader: suspend (K) -> V): V
    
    /**
     * 设置缓存值
     */
    suspend fun put(key: K, value: V)
    
    /**
     * 设置缓存值带过期时间
     */
    suspend fun put(key: K, value: V, ttl: Duration)
    
    /**
     * 移除缓存值
     */
    suspend fun remove(key: K): V?
    
    /**
     * 是否包含键
     */
    suspend fun contains(key: K): Boolean
    
    /**
     * 清空缓存
     */
    suspend fun clear()
    
    /**
     * 缓存大小
     */
    suspend fun size(): Long
    
    /**
     * 获取所有键
     */
    suspend fun keys(): Set<K>
}

/**
 * 分布式缓存接口
 */
interface DistributedCache<K, V> : Cache<K, V> {
    /**
     * 原子性设置（如果不存在）
     */
    suspend fun putIfAbsent(key: K, value: V): Boolean
    
    /**
     * 原子性设置带过期时间
     */
    suspend fun putIfAbsent(key: K, value: V, ttl: Duration): Boolean
    
    /**
     * 获取并设置
     */
    suspend fun getAndPut(key: K, value: V): V?
    
    /**
     * 比较并设置
     */
    suspend fun compareAndSet(key: K, expect: V, update: V): Boolean
    
    /**
     * 发布消息到缓存通道
     */
    suspend fun publish(channel: String, message: V)
    
    /**
     * 订阅缓存通道
     */
    fun subscribe(channel: String, handler: suspend (V) -> Unit)
}

/**
 * 缓存统计
 */
interface CacheStats {
    /** 命中次数 */
    val hitCount: Long
    
    /** 未命中次数 */
    val missCount: Long
    
    /** 命中率 */
    val hitRate: Double
    
    /** 驱逐次数 */
    val evictionCount: Long
    
    /** 加载次数 */
    val loadCount: Long
    
    /** 平均加载时间（纳秒） */
    val averageLoadPenalty: Long
}

/**
 * 带统计的缓存
 */
interface StatsCache<K, V> : Cache<K, V> {
    /** 获取缓存统计 */
    fun stats(): CacheStats
}
