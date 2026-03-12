package com.azathoth.core.events.subscriber

import com.azathoth.core.events.bus.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

/**
 * 基于 Kafka 的事件订阅器实现
 */
class KafkaEventSubscriber(
    bootstrapServers: String,
    override val groupId: String,
    override val subscriberId: String = UUID.randomUUID().toString(),
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
    private val pollTimeout: Duration = Duration.ofMillis(100),
    additionalConfig: Map<String, Any> = emptyMap()
) : EventSubscriber {

    private val consumer: KafkaConsumer<String, String>
    private val running = AtomicBoolean(false)
    private val paused = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineName("kafka-subscriber-$subscriberId"))

    override val isRunning: Boolean get() = running.get()

    private data class HandlerEntry(
        val eventClass: KClass<out Event>,
        val handler: suspend (ConsumeRecord<Event>) -> Unit,
        val errorHandler: (suspend (ConsumeRecord<Event>, Throwable) -> Unit)?
    )

    private val handlers = ConcurrentHashMap<String, HandlerEntry>()

    init {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            additionalConfig.forEach { (k, v) -> put(k, v) }
        }
        consumer = KafkaConsumer(props)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> subscribe(topic: String, eventClass: KClass<T>, handler: ConsumeHandler<T>) {
        handlers[topic] = HandlerEntry(
            eventClass = eventClass,
            handler = { record -> handler.handle(record as ConsumeRecord<T>) },
            errorHandler = { record, error -> handler.onError(record as ConsumeRecord<T>, error) }
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> subscribe(topic: String, eventClass: KClass<T>, handler: suspend (ConsumeRecord<T>) -> Unit) {
        handlers[topic] = HandlerEntry(
            eventClass = eventClass,
            handler = { record -> handler(record as ConsumeRecord<T>) },
            errorHandler = null
        )
    }

    override fun <T : Event> subscribe(topics: List<String>, eventClass: KClass<T>, handler: ConsumeHandler<T>) {
        topics.forEach { subscribe(it, eventClass, handler) }
    }

    override fun unsubscribe(topic: String) {
        handlers.remove(topic)
        refreshSubscription()
    }

    override fun unsubscribeAll() {
        handlers.clear()
        consumer.unsubscribe()
    }

    override suspend fun start() {
        if (running.getAndSet(true)) return
        refreshSubscription()

        scope.launch {
            while (running.get()) {
                if (paused.get()) {
                    delay(100)
                    continue
                }
                try {
                    val records = consumer.poll(pollTimeout)
                    for (record in records) {
                        val entry = handlers[record.topic()] ?: continue
                        try {
                            val event = deserializeEvent(record.value(), entry.eventClass)
                            val consumeRecord = SimpleConsumeRecord(
                                topic = record.topic(),
                                partition = record.partition(),
                                offset = record.offset(),
                                timestamp = record.timestamp(),
                                key = record.key(),
                                headers = record.headers().associate { it.key() to String(it.value()) },
                                event = event
                            )
                            @Suppress("UNCHECKED_CAST")
                            entry.handler(consumeRecord as ConsumeRecord<Event>)
                        } catch (e: Exception) {
                            logger.error(e) { "处理消息失败: topic=${record.topic()}, offset=${record.offset()}" }
                        }
                    }
                } catch (e: Exception) {
                    if (running.get()) {
                        logger.error(e) { "Kafka 消费循环异常" }
                        delay(1000)
                    }
                }
            }
        }
    }

    override suspend fun stop() {
        running.set(false)
        scope.cancel()
        consumer.close()
    }

    override fun pause() {
        paused.set(true)
    }

    override fun resume() {
        paused.set(false)
    }

    override suspend fun commit() {
        consumer.commitSync()
    }

    override suspend fun commit(topic: String, partition: Int, offset: Long) {
        val tp = TopicPartition(topic, partition)
        consumer.commitSync(mapOf(tp to OffsetAndMetadata(offset + 1)))
    }

    private fun refreshSubscription() {
        val topics = handlers.keys.toList()
        if (topics.isNotEmpty()) {
            consumer.subscribe(topics)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> deserializeEvent(jsonStr: String, eventClass: KClass<T>): T {
        val serializer = serializer(eventClass.java) as KSerializer<T>
        return json.decodeFromString(serializer, jsonStr)
    }
}

/**
 * 消费记录的简单实现
 */
data class SimpleConsumeRecord<T : Event>(
    override val topic: String,
    override val partition: Int,
    override val offset: Long,
    override val timestamp: Long,
    override val key: String?,
    override val headers: Map<String, String>,
    override val event: T
) : ConsumeRecord<T>

/**
 * 订阅配置的简单实现
 */
data class SimpleSubscribeConfig(
    override val groupId: String,
    override val enableAutoCommit: Boolean = false,
    override val autoCommitIntervalMs: Long = 5000,
    override val maxPollRecords: Int = 500,
    override val sessionTimeoutMs: Long = 30000,
    override val heartbeatIntervalMs: Long = 10000,
    override val autoOffsetReset: OffsetResetStrategy = OffsetResetStrategy.EARLIEST
) : SubscribeConfig
