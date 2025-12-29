package com.azathoth.core.events.bus

import kotlin.reflect.KClass

/**
 * 事件优先级
 */
enum class EventPriority(val value: Int) {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4),
    MONITOR(5)  // 仅监听，不应修改事件
}

/**
 * 基础事件接口
 */
interface Event {
    /** 事件ID */
    val eventId: String
    
    /** 事件时间戳 */
    val timestamp: Long
    
    /** 事件来源 */
    val source: String
}

/**
 * 可取消的事件
 */
interface CancellableEvent : Event {
    /** 是否已取消 */
    var cancelled: Boolean
}

/**
 * 异步事件标记
 */
interface AsyncEvent : Event

/**
 * 事件监听器
 */
interface EventListener<T : Event> {
    /** 监听的事件类型 */
    val eventClass: KClass<T>
    
    /** 优先级 */
    val priority: EventPriority get() = EventPriority.NORMAL
    
    /** 是否忽略已取消的事件 */
    val ignoreCancelled: Boolean get() = false
    
    /** 处理事件 */
    suspend fun onEvent(event: T)
}

/**
 * 事件总线
 */
interface EventBus {
    /**
     * 注册事件监听器
     */
    fun <T : Event> register(listener: EventListener<T>)
    
    /**
     * 注册事件监听器（函数式）
     */
    fun <T : Event> register(
        eventClass: KClass<T>,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        handler: suspend (T) -> Unit
    ): EventSubscription
    
    /**
     * 注销事件监听器
     */
    fun <T : Event> unregister(listener: EventListener<T>)
    
    /**
     * 注销指定类型的所有监听器
     */
    fun <T : Event> unregisterAll(eventClass: KClass<T>)
    
    /**
     * 发布事件（同步）
     */
    suspend fun <T : Event> publish(event: T): T
    
    /**
     * 发布事件（异步，不等待处理完成）
     */
    fun <T : Event> publishAsync(event: T)
    
    /**
     * 获取指定类型的监听器数量
     */
    fun <T : Event> getListenerCount(eventClass: KClass<T>): Int
}

/**
 * 事件订阅
 */
interface EventSubscription {
    /** 订阅ID */
    val id: String
    
    /** 是否已取消 */
    val isCancelled: Boolean
    
    /** 取消订阅 */
    fun cancel()
}

/**
 * 事件过滤器
 */
interface EventFilter<T : Event> {
    /** 过滤事件，返回 true 表示通过 */
    fun filter(event: T): Boolean
}
