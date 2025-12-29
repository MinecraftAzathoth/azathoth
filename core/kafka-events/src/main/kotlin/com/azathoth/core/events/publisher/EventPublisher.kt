package com.azathoth.core.events.publisher

import com.azathoth.core.events.bus.Event

/**
 * 事件发布配置
 */
interface PublishConfig {
    /** 目标主题 */
    val topic: String
    
    /** 分区键（用于保证顺序） */
    val partitionKey: String?
    
    /** 消息头 */
    val headers: Map<String, String>
    
    /** 是否等待确认 */
    val waitForAck: Boolean
    
    /** 超时时间（毫秒） */
    val timeoutMs: Long
}

/**
 * 发布结果
 */
interface PublishResult {
    /** 是否成功 */
    val success: Boolean
    
    /** 目标主题 */
    val topic: String
    
    /** 分区 */
    val partition: Int
    
    /** 偏移量 */
    val offset: Long
    
    /** 时间戳 */
    val timestamp: Long
    
    /** 错误信息 */
    val error: Throwable?
}

/**
 * 事件发布器
 */
interface EventPublisher {
    /**
     * 发布事件到默认主题
     */
    suspend fun <T : Event> publish(event: T): PublishResult
    
    /**
     * 发布事件到指定主题
     */
    suspend fun <T : Event> publish(event: T, topic: String): PublishResult
    
    /**
     * 发布事件（带配置）
     */
    suspend fun <T : Event> publish(event: T, config: PublishConfig): PublishResult
    
    /**
     * 批量发布事件
     */
    suspend fun <T : Event> publishBatch(events: List<T>, topic: String): List<PublishResult>
    
    /**
     * 异步发布（不等待结果）
     */
    fun <T : Event> publishAsync(event: T, topic: String)
    
    /**
     * 关闭发布器
     */
    suspend fun close()
}

/**
 * 事件主题配置
 */
interface TopicConfig {
    /** 主题名称 */
    val name: String
    
    /** 分区数 */
    val partitions: Int
    
    /** 副本因子 */
    val replicationFactor: Short
    
    /** 保留时间（毫秒） */
    val retentionMs: Long
    
    /** 额外配置 */
    val config: Map<String, String>
}

/**
 * 主题管理器
 */
interface TopicManager {
    /** 创建主题 */
    suspend fun createTopic(config: TopicConfig)
    
    /** 删除主题 */
    suspend fun deleteTopic(name: String)
    
    /** 检查主题是否存在 */
    suspend fun topicExists(name: String): Boolean
    
    /** 获取所有主题 */
    suspend fun listTopics(): List<String>
    
    /** 获取主题配置 */
    suspend fun getTopicConfig(name: String): TopicConfig?
}
