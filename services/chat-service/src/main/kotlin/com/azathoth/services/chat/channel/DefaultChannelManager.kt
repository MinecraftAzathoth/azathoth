package com.azathoth.services.chat.channel

import com.azathoth.core.common.identity.PlayerId
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

class DefaultChannelManager : ChannelManager {

    private val channels = ConcurrentHashMap<String, ChatChannel>()
    private val defaultChannelId: String

    init {
        val worldConfig = SimpleChannelConfig(
            name = "世界频道",
            type = ChannelType.WORLD,
            persistent = true,
            cooldownSeconds = 3
        )
        val worldChannel = DefaultChatChannel(channelId = "world", config = worldConfig)
        channels["world"] = worldChannel
        defaultChannelId = "world"
        logger.info { "默认世界频道已创建" }
    }

    override suspend fun createChannel(config: ChannelConfig): ChatChannel {
        val channelId = UUID.randomUUID().toString()
        val channel = DefaultChatChannel(channelId = channelId, config = config)
        channels[channelId] = channel
        logger.info { "创建频道: ${config.name} ($channelId), 类型: ${config.type}" }
        return channel
    }

    override suspend fun deleteChannel(channelId: String) {
        if (channelId == defaultChannelId) {
            logger.warn { "不能删除默认频道" }
            return
        }
        channels.remove(channelId)
        logger.info { "删除频道: $channelId" }
    }

    override fun getChannel(channelId: String): ChatChannel? = channels[channelId]

    override fun getChannelsByType(type: ChannelType): List<ChatChannel> =
        channels.values.filter { it.type == type }

    override fun getPlayerChannels(playerId: PlayerId): List<ChatChannel> =
        channels.values.filter { it.isMember(playerId) }

    override fun getDefaultChannel(): ChatChannel = channels[defaultChannelId]!!
}
