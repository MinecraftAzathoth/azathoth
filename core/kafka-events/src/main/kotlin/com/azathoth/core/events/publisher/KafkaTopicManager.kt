package com.azathoth.core.events.publisher

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import java.util.Properties

private val logger = KotlinLogging.logger {}

/**
 * 基于 Kafka AdminClient 的主题管理器实现
 */
class KafkaTopicManager(
    bootstrapServers: String
) : TopicManager {

    private val adminClient: AdminClient

    init {
        val props = Properties().apply {
            put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
        }
        adminClient = AdminClient.create(props)
    }

    override suspend fun createTopic(config: TopicConfig) {
        val newTopic = NewTopic(config.name, config.partitions, config.replicationFactor)
        val topicConfig = config.config.toMutableMap()
        if (config.retentionMs > 0) {
            topicConfig["retention.ms"] = config.retentionMs.toString()
        }
        newTopic.configs(topicConfig)

        try {
            adminClient.createTopics(listOf(newTopic)).all().get()
            logger.info { "主题创建成功: ${config.name}" }
        } catch (e: Exception) {
            logger.error(e) { "主题创建失败: ${config.name}" }
            throw e
        }
    }

    override suspend fun deleteTopic(name: String) {
        adminClient.deleteTopics(listOf(name)).all().get()
        logger.info { "主题删除成功: $name" }
    }

    override suspend fun topicExists(name: String): Boolean {
        val topics = adminClient.listTopics().names().get()
        return name in topics
    }

    override suspend fun listTopics(): List<String> {
        return adminClient.listTopics().names().get().toList()
    }

    override suspend fun getTopicConfig(name: String): TopicConfig? {
        if (!topicExists(name)) return null
        val description = adminClient.describeTopics(listOf(name)).allTopicNames().get()[name] ?: return null
        return SimpleTopicConfig(
            name = name,
            partitions = description.partitions().size,
            replicationFactor = description.partitions().firstOrNull()?.replicas()?.size?.toShort() ?: 1,
            retentionMs = -1,
            config = emptyMap()
        )
    }

    fun close() {
        adminClient.close()
    }
}

/**
 * TopicConfig 的简单实现
 */
data class SimpleTopicConfig(
    override val name: String,
    override val partitions: Int = 1,
    override val replicationFactor: Short = 1,
    override val retentionMs: Long = 604_800_000, // 7 天
    override val config: Map<String, String> = emptyMap()
) : TopicConfig
