package com.azathoth.services.chat.channel

import com.azathoth.core.common.identity.PlayerId
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

data class SimpleChatMessage(
    override val messageId: String,
    override val channelId: String,
    override val senderId: PlayerId,
    override val senderName: String,
    override val content: String,
    override val type: MessageType = MessageType.TEXT,
    override val sentAt: Long = System.currentTimeMillis(),
    override val metadata: Map<String, Any> = emptyMap()
) : ChatMessage

data class SimpleChatPermissionResult(
    override val allowed: Boolean,
    override val denyReason: ChatDenyReason? = null,
    override val cooldownRemaining: Int? = null
) : ChatPermissionResult

data class SimpleChannelConfig(
    override val name: String,
    override val type: ChannelType,
    override val persistent: Boolean = false,
    override val messageRetentionSeconds: Long = 3600,
    override val cooldownSeconds: Int = 0,
    override val minLevelRequired: Int = 0,
    override val permissionRequired: String? = null,
    override val maxMembers: Int? = null
) : ChannelConfig

class DefaultChatChannel(
    override val channelId: String,
    config: ChannelConfig
) : ChatChannel {

    override val name: String = config.name
    override val type: ChannelType = config.type
    override val persistent: Boolean = config.persistent
    override val messageRetentionSeconds: Long = config.messageRetentionSeconds
    override val cooldownSeconds: Int = config.cooldownSeconds
    override val minLevelRequired: Int = config.minLevelRequired
    override val permissionRequired: String? = config.permissionRequired

    private val members = ConcurrentHashMap.newKeySet<PlayerId>()
    private val mutedPlayers = ConcurrentHashMap.newKeySet<PlayerId>()
    /** playerId -> 上次发言时间戳(ms) */
    private val lastSpeakTime = ConcurrentHashMap<String, Long>()

    override fun getMembers(): Set<PlayerId> = members.toSet()

    override val memberCount: Int get() = members.size

    override suspend fun addMember(playerId: PlayerId) {
        members.add(playerId)
        logger.debug { "玩家 ${playerId.value} 加入频道 $name" }
    }

    override suspend fun removeMember(playerId: PlayerId) {
        members.remove(playerId)
        lastSpeakTime.remove(playerId.value)
        logger.debug { "玩家 ${playerId.value} 离开频道 $name" }
    }

    override fun isMember(playerId: PlayerId): Boolean = members.contains(playerId)

    override fun canSpeak(playerId: PlayerId): ChatPermissionResult {
        if (!isMember(playerId)) {
            return SimpleChatPermissionResult(allowed = false, denyReason = ChatDenyReason.NOT_MEMBER)
        }
        if (mutedPlayers.contains(playerId)) {
            return SimpleChatPermissionResult(allowed = false, denyReason = ChatDenyReason.MUTED)
        }
        if (cooldownSeconds > 0) {
            val last = lastSpeakTime[playerId.value]
            if (last != null) {
                val elapsed = (System.currentTimeMillis() - last) / 1000
                if (elapsed < cooldownSeconds) {
                    val remaining = cooldownSeconds - elapsed.toInt()
                    return SimpleChatPermissionResult(
                        allowed = false,
                        denyReason = ChatDenyReason.ON_COOLDOWN,
                        cooldownRemaining = remaining
                    )
                }
            }
        }
        return SimpleChatPermissionResult(allowed = true)
    }

    override suspend fun broadcast(message: ChatMessage) {
        lastSpeakTime[message.senderId.value] = System.currentTimeMillis()
        logger.debug { "[${name}] ${message.senderName}: ${message.content}" }
    }

    /** 禁言玩家 */
    fun mutePlayer(playerId: PlayerId) {
        mutedPlayers.add(playerId)
    }

    /** 解除禁言 */
    fun unmutePlayer(playerId: PlayerId) {
        mutedPlayers.remove(playerId)
    }
}
