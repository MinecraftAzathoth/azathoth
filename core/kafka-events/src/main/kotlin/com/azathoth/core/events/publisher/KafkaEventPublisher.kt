package com.azathoth.core.events.publisher

import com.azathoth.core.events.bus.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

private val logger = KotlinLogging.logger {}

/**
 * 基于 Kafka 的事件发布器实现
 */
class KafkaEventPublisher(
    bootstrapServers: String,
    private val defaultTopic: String = "azathoth-events",
    private val json: Json = Json { encodeDefaults = true; ignoreUnknownKeys = true },
    additionalConfig: Map<String, Any> = emptyMap()
) : EventPublisher {

    private val producer: KafkaProducer<String, String>
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.RETRIES_CONFIG, 3)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true)
            additionalConfig.forEach { (k, v) -> put(k, v) }
        }
        producer = KafkaProducer(props)
    }

    override suspend fun <T : Event> publish(event: T): PublishResult =
        publish(event, defaultTopic)

    override suspend fun <T : Event> publish(event: T, topic: String): PublishResult {
        return doPublish(event, topic, event.eventId, emptyMap())
    }

    override suspend fun <T : Event> publish(event: T, config: PublishConfig): PublishResult {
        return doPublish(event, config.topic, config.partitionKey, config.headers)
    }

    override suspend fun <T : Event> publishBatch(events: List<T>, topic: String): List<PublishResult> {
        return events.map { publish(it, topic) }
    }

    override fun <T : Event> publishAsync(event: T, topic: String) {
        scope.launch {
            try {
                publish(event, topic)
            } catch (e: Exception) {
                logger.error(e) { "异步发布事件失败: ${event.eventId}" }
            }
        }
    }

    override suspend fun close() {
        producer.close()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : Event> doPublish(
        event: T,
        topic: String,
        key: String?,
        headers: Map<String, String>
    ): PublishResult {
        return try {
            val serializer = serializer(event::class.java) as KSerializer<T>
            val jsonStr = json.encodeToString(serializer, event)

            val record = ProducerRecord<String, String>(topic, null, key, jsonStr)
            headers.forEach { (k, v) ->
                record.headers().add(RecordHeader(k, v.toByteArray()))
            }

            val metadata = producer.send(record).await()

            SimplePublishResult(
                success = true,
                topic = metadata.topic(),
                partition = metadata.partition(),
                offset = metadata.offset(),
                timestamp = metadata.timestamp(),
                error = null
            )
        } catch (e: Exception) {
            logger.error(e) { "发布事件失败: ${event.eventId} → $topic" }
            SimplePublishResult(
                success = false,
                topic = topic,
                partition = -1,
                offset = -1,
                timestamp = System.currentTimeMillis(),
                error = e
            )
        }
    }
}

/**
 * 发布配置的简单实现
 */
data class SimplePublishConfig(
    override val topic: String,
    override val partitionKey: String? = null,
    override val headers: Map<String, String> = emptyMap(),
    override val waitForAck: Boolean = true,
    override val timeoutMs: Long = 30_000
) : PublishConfig

/**
 * 发布结果的简单实现
 */
data class SimplePublishResult(
    override val success: Boolean,
    override val topic: String,
    override val partition: Int,
    override val offset: Long,
    override val timestamp: Long,
    override val error: Throwable?
) : PublishResult
