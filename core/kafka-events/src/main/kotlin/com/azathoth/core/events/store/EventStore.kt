package com.azathoth.core.events.store

import com.azathoth.core.events.bus.Event
import kotlin.reflect.KClass

/**
 * 存储的事件记录
 */
interface StoredEvent<T : Event> {
    /** 事件ID */
    val eventId: String
    
    /** 事件类型 */
    val eventType: String
    
    /** 聚合ID */
    val aggregateId: String
    
    /** 聚合类型 */
    val aggregateType: String
    
    /** 版本号 */
    val version: Long
    
    /** 事件数据 */
    val event: T
    
    /** 元数据 */
    val metadata: Map<String, String>
    
    /** 创建时间 */
    val createdAt: Long
}

/**
 * 事件存储
 */
interface EventStore {
    /**
     * 追加事件
     */
    suspend fun <T : Event> append(
        aggregateType: String,
        aggregateId: String,
        event: T,
        expectedVersion: Long? = null
    ): StoredEvent<T>
    
    /**
     * 批量追加事件
     */
    suspend fun <T : Event> appendBatch(
        aggregateType: String,
        aggregateId: String,
        events: List<T>,
        expectedVersion: Long? = null
    ): List<StoredEvent<T>>
    
    /**
     * 读取聚合的所有事件
     */
    suspend fun <T : Event> readEvents(
        aggregateType: String,
        aggregateId: String,
        eventClass: KClass<T>
    ): List<StoredEvent<T>>
    
    /**
     * 读取聚合的事件（从指定版本开始）
     */
    suspend fun <T : Event> readEvents(
        aggregateType: String,
        aggregateId: String,
        fromVersion: Long,
        eventClass: KClass<T>
    ): List<StoredEvent<T>>
    
    /**
     * 读取聚合的最新版本
     */
    suspend fun getLatestVersion(
        aggregateType: String,
        aggregateId: String
    ): Long?
    
    /**
     * 读取全局事件流
     */
    suspend fun <T : Event> readAllEvents(
        eventClass: KClass<T>,
        fromPosition: Long = 0,
        limit: Int = 100
    ): List<StoredEvent<T>>
}

/**
 * 事件快照
 */
interface EventSnapshot<T> {
    /** 聚合ID */
    val aggregateId: String
    
    /** 聚合类型 */
    val aggregateType: String
    
    /** 快照版本 */
    val version: Long
    
    /** 快照数据 */
    val data: T
    
    /** 创建时间 */
    val createdAt: Long
}

/**
 * 快照存储
 */
interface SnapshotStore {
    /**
     * 保存快照
     */
    suspend fun <T : Any> saveSnapshot(
        aggregateType: String,
        aggregateId: String,
        version: Long,
        data: T
    )
    
    /**
     * 获取最新快照
     */
    suspend fun <T : Any> getLatestSnapshot(
        aggregateType: String,
        aggregateId: String,
        dataClass: KClass<T>
    ): EventSnapshot<T>?
    
    /**
     * 删除旧快照
     */
    suspend fun deleteOldSnapshots(
        aggregateType: String,
        aggregateId: String,
        keepCount: Int = 1
    )
}
