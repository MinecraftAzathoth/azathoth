package com.azathoth.core.events.bus

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

/**
 * 本地事件总线实现 — 支持优先级、取消、异步、过滤器
 */
class LocalEventBus(
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("event-bus")
    )
) : EventBus {

    private data class ListenerEntry(
        val id: String,
        val eventClassName: String,
        val priority: EventPriority,
        val ignoreCancelled: Boolean,
        val handler: suspend (Event) -> Unit
    )

    private val listeners = ConcurrentHashMap<String, CopyOnWriteArrayList<ListenerEntry>>()

    override fun <T : Event> register(listener: EventListener<T>) {
        val className = listener.eventClass.qualifiedName ?: return
        val entry = ListenerEntry(
            id = UUID.randomUUID().toString(),
            eventClassName = className,
            priority = listener.priority,
            ignoreCancelled = listener.ignoreCancelled,
            handler = @Suppress("UNCHECKED_CAST") { event -> listener.onEvent(event as T) }
        )
        listeners.getOrPut(className) { CopyOnWriteArrayList() }.add(entry)
        sortListeners(className)
    }

    override fun <T : Event> register(
        eventClass: KClass<T>,
        priority: EventPriority,
        ignoreCancelled: Boolean,
        handler: suspend (T) -> Unit
    ): EventSubscription {
        val className = eventClass.qualifiedName ?: throw IllegalArgumentException("事件类型无法获取名称")
        val id = UUID.randomUUID().toString()
        val entry = ListenerEntry(
            id = id,
            eventClassName = className,
            priority = priority,
            ignoreCancelled = ignoreCancelled,
            handler = @Suppress("UNCHECKED_CAST") { event -> handler(event as T) }
        )
        listeners.getOrPut(className) { CopyOnWriteArrayList() }.add(entry)
        sortListeners(className)

        return object : EventSubscription {
            override val id: String = id
            override var isCancelled: Boolean = false
                private set

            override fun cancel() {
                isCancelled = true
                listeners[className]?.removeIf { it.id == id }
            }
        }
    }

    override fun <T : Event> unregister(listener: EventListener<T>) {
        val className = listener.eventClass.qualifiedName ?: return
        listeners[className]?.removeIf { it.handler === listener }
    }

    override fun <T : Event> unregisterAll(eventClass: KClass<T>) {
        val className = eventClass.qualifiedName ?: return
        listeners.remove(className)
    }

    override suspend fun <T : Event> publish(event: T): T {
        val className = event::class.qualifiedName ?: return event
        val entries = listeners[className] ?: return event

        for (entry in entries) {
            if (event is CancellableEvent && event.cancelled && entry.ignoreCancelled) {
                continue
            }
            try {
                entry.handler(event)
            } catch (e: Exception) {
                logger.error(e) { "事件处理器异常: ${entry.id}" }
            }
        }
        return event
    }

    override fun <T : Event> publishAsync(event: T) {
        scope.launch {
            try {
                publish(event)
            } catch (e: Exception) {
                logger.error(e) { "异步事件发布异常" }
            }
        }
    }

    override fun <T : Event> getListenerCount(eventClass: KClass<T>): Int {
        val className = eventClass.qualifiedName ?: return 0
        return listeners[className]?.size ?: 0
    }

    private fun sortListeners(className: String) {
        listeners[className]?.sortWith(compareBy { it.priority.value })
    }
}
