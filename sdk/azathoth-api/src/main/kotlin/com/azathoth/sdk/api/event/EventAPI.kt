package com.azathoth.sdk.api.event

import kotlin.reflect.KClass

/**
 * 事件优先级
 */
enum class Priority(val value: Int) {
    LOWEST(0),
    LOW(1),
    NORMAL(2),
    HIGH(3),
    HIGHEST(4),
    MONITOR(5)
}

/**
 * 事件基类
 */
interface AzathothEvent {
    /** 事件名称 */
    val eventName: String get() = this::class.simpleName ?: "UnknownEvent"
    
    /** 是否异步事件 */
    val isAsync: Boolean get() = false
}

/**
 * 可取消的事件
 */
interface Cancellable {
    /** 是否已取消 */
    var isCancelled: Boolean
}

/**
 * 事件处理器注解
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler(
    /** 优先级 */
    val priority: Priority = Priority.NORMAL,
    /** 是否忽略已取消的事件 */
    val ignoreCancelled: Boolean = false
)

/**
 * 事件监听器标记接口
 */
interface Listener

/**
 * 事件管理器 API
 */
interface EventManager {
    /**
     * 注册监听器
     */
    fun registerListener(listener: Listener, plugin: Any)
    
    /**
     * 注册单个事件处理器
     */
    fun <T : AzathothEvent> registerHandler(
        eventClass: KClass<T>,
        priority: Priority = Priority.NORMAL,
        ignoreCancelled: Boolean = false,
        handler: suspend (T) -> Unit
    ): EventSubscription
    
    /**
     * 注销监听器
     */
    fun unregisterListener(listener: Listener)
    
    /**
     * 注销插件的所有监听器
     */
    fun unregisterAll(plugin: Any)
    
    /**
     * 触发事件
     */
    suspend fun <T : AzathothEvent> call(event: T): T
    
    /**
     * 异步触发事件
     */
    fun <T : AzathothEvent> callAsync(event: T)
}

/**
 * 事件订阅
 */
interface EventSubscription {
    /** 是否已取消 */
    val isActive: Boolean
    
    /** 取消订阅 */
    fun unsubscribe()
}

/**
 * 玩家事件基类
 */
interface PlayerEvent : AzathothEvent {
    /** 玩家标识 */
    val playerId: String
}

/**
 * 玩家加入事件
 */
interface PlayerJoinEvent : PlayerEvent {
    /** 加入消息 */
    var joinMessage: String?
}

/**
 * 玩家离开事件
 */
interface PlayerQuitEvent : PlayerEvent {
    /** 离开消息 */
    var quitMessage: String?
    
    /** 离开原因 */
    val reason: QuitReason
}

/**
 * 离开原因
 */
enum class QuitReason {
    DISCONNECTED,
    KICKED,
    TIMED_OUT,
    TRANSFER
}

/**
 * 玩家聊天事件
 */
interface PlayerChatEvent : PlayerEvent, Cancellable {
    /** 消息内容 */
    var message: String
    
    /** 聊天格式 */
    var format: String
    
    /** 接收者列表 */
    val recipients: MutableSet<String>
}

/**
 * 玩家移动事件
 */
interface PlayerMoveEvent : PlayerEvent, Cancellable {
    /** 起始位置 */
    val from: Location
    
    /** 目标位置 */
    var to: Location
}

/**
 * 位置数据
 */
data class Location(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float = 0f,
    val pitch: Float = 0f
)
