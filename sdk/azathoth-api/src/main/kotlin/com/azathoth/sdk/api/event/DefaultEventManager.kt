package com.azathoth.sdk.api.event

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaMethod

private val logger = KotlinLogging.logger {}

/**
 * 已注册的事件处理器
 */
internal data class RegisteredHandler(
    val eventClass: KClass<out AzathothEvent>,
    val priority: Priority,
    val ignoreCancelled: Boolean,
    val handler: suspend (AzathothEvent) -> Unit,
    val plugin: Any?,
    val listener: Listener?,
    val subscription: DefaultEventSubscription
)

/**
 * 默认事件订阅实现
 */
class DefaultEventSubscription(
    private val onUnsubscribe: () -> Unit
) : EventSubscription {
    override var isActive: Boolean = true
        private set

    override fun unsubscribe() {
        if (isActive) {
            isActive = false
            onUnsubscribe()
        }
    }
}

/**
 * 默认事件管理器实现
 */
class DefaultEventManager : EventManager {

    private val handlers = ConcurrentHashMap<KClass<out AzathothEvent>, MutableList<RegisteredHandler>>()
    private val listenerPluginMap = ConcurrentHashMap<Listener, Any>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun registerListener(listener: Listener, plugin: Any) {
        listenerPluginMap[listener] = plugin

        val kClass = listener::class
        for (func in kClass.memberFunctions) {
            val annotation = func.findAnnotation<EventHandler>() ?: continue
            val params = func.parameters
            // params[0] is `this`, params[1] should be the event
            if (params.size != 2) {
                logger.warn { "事件处理方法 ${func.name} 参数数量不正确，跳过" }
                continue
            }

            val eventParam = params[1]
            val eventClass = eventParam.type.classifier as? KClass<out AzathothEvent>
            if (eventClass == null) {
                logger.warn { "事件处理方法 ${func.name} 参数类型不是 AzathothEvent，跳过" }
                continue
            }

            val javaMethod = func.javaMethod ?: continue
            javaMethod.isAccessible = true

            val isSuspend = func.isSuspend

            @Suppress("UNCHECKED_CAST")
            val handler: suspend (AzathothEvent) -> Unit = { event ->
                if (isSuspend) {
                    kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn<Any?> { cont ->
                        javaMethod.invoke(listener, event, cont)
                    }
                } else {
                    javaMethod.invoke(listener, event)
                }
            }

            val subscription = DefaultEventSubscription {}
            val registered = RegisteredHandler(
                eventClass = eventClass,
                priority = annotation.priority,
                ignoreCancelled = annotation.ignoreCancelled,
                handler = handler,
                plugin = plugin,
                listener = listener,
                subscription = subscription
            )

            handlers.getOrPut(eventClass) { mutableListOf() }.apply {
                add(registered)
                sortBy { it.priority.value }
            }

            logger.debug { "注册事件处理器: ${eventClass.simpleName} <- ${kClass.simpleName}.${func.name} [${annotation.priority}]" }
        }
    }

    override fun <T : AzathothEvent> registerHandler(
        eventClass: KClass<T>,
        priority: Priority,
        ignoreCancelled: Boolean,
        handler: suspend (T) -> Unit
    ): EventSubscription {
        lateinit var registered: RegisteredHandler
        val subscription = DefaultEventSubscription {
            handlers[eventClass]?.remove(registered)
        }

        @Suppress("UNCHECKED_CAST")
        registered = RegisteredHandler(
            eventClass = eventClass,
            priority = priority,
            ignoreCancelled = ignoreCancelled,
            handler = handler as suspend (AzathothEvent) -> Unit,
            plugin = null,
            listener = null,
            subscription = subscription
        )

        handlers.getOrPut(eventClass) { mutableListOf() }.apply {
            add(registered)
            sortBy { it.priority.value }
        }

        return subscription
    }

    override fun unregisterListener(listener: Listener) {
        listenerPluginMap.remove(listener)
        for ((_, list) in handlers) {
            list.removeAll { it.listener === listener }
        }
    }

    override fun unregisterAll(plugin: Any) {
        val listenersToRemove = listenerPluginMap.entries.filter { it.value === plugin }.map { it.key }
        listenersToRemove.forEach { listenerPluginMap.remove(it) }

        for ((_, list) in handlers) {
            list.removeAll { it.plugin === plugin }
        }
    }

    override suspend fun <T : AzathothEvent> call(event: T): T {
        val eventHandlers = handlers[event::class] ?: return event

        // 创建快照避免并发修改
        val snapshot = synchronized(eventHandlers) { eventHandlers.toList() }

        for (handler in snapshot) {
            if (!handler.subscription.isActive) continue

            if (event is Cancellable && event.isCancelled && handler.ignoreCancelled) {
                continue
            }

            try {
                handler.handler(event)
            } catch (e: Exception) {
                logger.error(e) { "事件处理器执行异常: ${event.eventName}" }
            }
        }

        return event
    }

    override fun <T : AzathothEvent> callAsync(event: T) {
        scope.launch {
            call(event)
        }
    }
}
