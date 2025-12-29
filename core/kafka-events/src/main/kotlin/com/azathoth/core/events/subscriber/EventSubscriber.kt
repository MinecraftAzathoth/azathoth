package com.azathoth.core.events.subscriber

import com.azathoth.core.events.bus.Event
import kotlin.reflect.KClass

/**
 * 消费记录
 */
interface ConsumeRecord<T : Event> {
    /** 主题 */
    val topic: String
    
    /** 分区 */
    val partition: Int
    
    /** 偏移量 */
    val offset: Long
    
    /** 时间戳 */
    val timestamp: Long
    
    /** 消息键 */
    val key: String?
    
    /** 消息头 */
    val headers: Map<String, String>
    
    /** 事件数据 */
    val event: T
}

/**
 * 消费处理器
 */
interface ConsumeHandler<T : Event> {
    /** 处理的事件类型 */
    val eventClass: KClass<T>
    
    /** 处理消费记录 */
    suspend fun handle(record: ConsumeRecord<T>)
    
    /** 处理异常 */
    suspend fun onError(record: ConsumeRecord<T>, error: Throwable)
}

/**
 * 事件订阅器
 */
interface EventSubscriber {
    /** 订阅器ID */
    val subscriberId: String
    
    /** 消费者组ID */
    val groupId: String
    
    /** 是否正在运行 */
    val isRunning: Boolean
    
    /**
     * 订阅主题
     */
    fun <T : Event> subscribe(
        topic: String,
        eventClass: KClass<T>,
        handler: ConsumeHandler<T>
    )
    
    /**
     * 订阅主题（函数式）
     */
    fun <T : Event> subscribe(
        topic: String,
        eventClass: KClass<T>,
        handler: suspend (ConsumeRecord<T>) -> Unit
    )
    
    /**
     * 订阅多个主题
     */
    fun <T : Event> subscribe(
        topics: List<String>,
        eventClass: KClass<T>,
        handler: ConsumeHandler<T>
    )
    
    /**
     * 取消订阅
     */
    fun unsubscribe(topic: String)
    
    /**
     * 取消所有订阅
     */
    fun unsubscribeAll()
    
    /**
     * 启动消费
     */
    suspend fun start()
    
    /**
     * 停止消费
     */
    suspend fun stop()
    
    /**
     * 暂停消费
     */
    fun pause()
    
    /**
     * 恢复消费
     */
    fun resume()
    
    /**
     * 提交偏移量
     */
    suspend fun commit()
    
    /**
     * 提交指定偏移量
     */
    suspend fun commit(topic: String, partition: Int, offset: Long)
}

/**
 * 订阅配置
 */
interface SubscribeConfig {
    /** 消费者组ID */
    val groupId: String
    
    /** 自动提交 */
    val enableAutoCommit: Boolean
    
    /** 自动提交间隔（毫秒） */
    val autoCommitIntervalMs: Long
    
    /** 最大拉取记录数 */
    val maxPollRecords: Int
    
    /** 会话超时（毫秒） */
    val sessionTimeoutMs: Long
    
    /** 心跳间隔（毫秒） */
    val heartbeatIntervalMs: Long
    
    /** 从头开始消费 */
    val autoOffsetReset: OffsetResetStrategy
}

/**
 * 偏移量重置策略
 */
enum class OffsetResetStrategy {
    EARLIEST,
    LATEST,
    NONE
}
