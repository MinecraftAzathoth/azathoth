package com.azathoth.services.chat.channel

import com.azathoth.core.common.identity.PlayerId

/**
 * 频道类型
 */
enum class ChannelType {
    /** 世界频道 */
    WORLD,
    /** 区域频道 */
    LOCAL,
    /** 公会频道 */
    GUILD,
    /** 队伍频道 */
    PARTY,
    /** 私聊 */
    PRIVATE,
    /** 系统频道 */
    SYSTEM,
    /** 交易频道 */
    TRADE,
    /** 招募频道 */
    RECRUIT,
    /** 自定义频道 */
    CUSTOM
}

/**
 * 聊天频道
 */
interface ChatChannel {
    /** 频道ID */
    val channelId: String
    
    /** 频道名称 */
    val name: String
    
    /** 频道类型 */
    val type: ChannelType
    
    /** 是否持久化消息 */
    val persistent: Boolean
    
    /** 消息保留时间（秒） */
    val messageRetentionSeconds: Long
    
    /** 发言冷却（秒） */
    val cooldownSeconds: Int
    
    /** 最小等级要求 */
    val minLevelRequired: Int
    
    /** 是否需要特定权限 */
    val permissionRequired: String?
    
    /** 频道成员 */
    fun getMembers(): Set<PlayerId>
    
    /** 成员数量 */
    val memberCount: Int
    
    /**
     * 添加成员
     */
    suspend fun addMember(playerId: PlayerId)
    
    /**
     * 移除成员
     */
    suspend fun removeMember(playerId: PlayerId)
    
    /**
     * 检查是否是成员
     */
    fun isMember(playerId: PlayerId): Boolean
    
    /**
     * 检查是否可以发言
     */
    fun canSpeak(playerId: PlayerId): ChatPermissionResult
    
    /**
     * 广播消息
     */
    suspend fun broadcast(message: ChatMessage)
}

/**
 * 聊天权限结果
 */
interface ChatPermissionResult {
    /** 是否允许 */
    val allowed: Boolean
    
    /** 拒绝原因 */
    val denyReason: ChatDenyReason?
    
    /** 冷却剩余时间（秒） */
    val cooldownRemaining: Int?
}

/**
 * 聊天拒绝原因
 */
enum class ChatDenyReason {
    /** 等级不足 */
    LEVEL_TOO_LOW,
    /** 权限不足 */
    NO_PERMISSION,
    /** 被禁言 */
    MUTED,
    /** 冷却中 */
    ON_COOLDOWN,
    /** 不是成员 */
    NOT_MEMBER,
    /** 频道已满 */
    CHANNEL_FULL,
    /** 频道已关闭 */
    CHANNEL_CLOSED
}

/**
 * 聊天消息
 */
interface ChatMessage {
    /** 消息ID */
    val messageId: String
    
    /** 频道ID */
    val channelId: String
    
    /** 发送者ID */
    val senderId: PlayerId
    
    /** 发送者名称 */
    val senderName: String
    
    /** 消息内容 */
    val content: String
    
    /** 消息类型 */
    val type: MessageType
    
    /** 发送时间 */
    val sentAt: Long
    
    /** 元数据 */
    val metadata: Map<String, Any>
}

/**
 * 消息类型
 */
enum class MessageType {
    /** 普通文本 */
    TEXT,
    /** 系统消息 */
    SYSTEM,
    /** 物品链接 */
    ITEM_LINK,
    /** 玩家链接 */
    PLAYER_LINK,
    /** 位置链接 */
    LOCATION_LINK,
    /** 表情 */
    EMOTE
}

/**
 * 频道管理器
 */
interface ChannelManager {
    /**
     * 创建频道
     */
    suspend fun createChannel(config: ChannelConfig): ChatChannel
    
    /**
     * 删除频道
     */
    suspend fun deleteChannel(channelId: String)
    
    /**
     * 获取频道
     */
    fun getChannel(channelId: String): ChatChannel?
    
    /**
     * 获取频道（按类型）
     */
    fun getChannelsByType(type: ChannelType): List<ChatChannel>
    
    /**
     * 获取玩家所在的频道
     */
    fun getPlayerChannels(playerId: PlayerId): List<ChatChannel>
    
    /**
     * 获取默认频道
     */
    fun getDefaultChannel(): ChatChannel
}

/**
 * 频道配置
 */
interface ChannelConfig {
    val name: String
    val type: ChannelType
    val persistent: Boolean
    val messageRetentionSeconds: Long
    val cooldownSeconds: Int
    val minLevelRequired: Int
    val permissionRequired: String?
    val maxMembers: Int?
}
