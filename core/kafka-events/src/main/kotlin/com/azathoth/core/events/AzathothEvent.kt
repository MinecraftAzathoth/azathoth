package com.azathoth.core.events

import kotlinx.serialization.Serializable
import java.time.Instant

/**
 * 事件基类
 */
@Serializable
sealed class AzathothEvent {
    abstract val eventId: String
    abstract val timestamp: Long
    abstract val source: String
}

/**
 * 玩家事件基类
 */
@Serializable
sealed class PlayerEvent : AzathothEvent() {
    abstract val playerId: String
}

/**
 * 玩家加入事件
 */
@Serializable
data class PlayerJoinEvent(
    override val eventId: String,
    override val timestamp: Long,
    override val source: String,
    override val playerId: String,
    val playerName: String,
    val gatewayId: String,
    val instanceId: String
) : PlayerEvent()

/**
 * 玩家离开事件
 */
@Serializable
data class PlayerLeaveEvent(
    override val eventId: String,
    override val timestamp: Long,
    override val source: String,
    override val playerId: String,
    val reason: String
) : PlayerEvent()

/**
 * 玩家传送事件
 */
@Serializable
data class PlayerTransferEvent(
    override val eventId: String,
    override val timestamp: Long,
    override val source: String,
    override val playerId: String,
    val fromInstance: String,
    val toInstance: String
) : PlayerEvent()

/**
 * 游戏实例事件基类
 */
@Serializable
sealed class InstanceEvent : AzathothEvent() {
    abstract val instanceId: String
}

/**
 * 实例启动事件
 */
@Serializable
data class InstanceStartEvent(
    override val eventId: String,
    override val timestamp: Long,
    override val source: String,
    override val instanceId: String,
    val instanceType: String,
    val maxPlayers: Int
) : InstanceEvent()

/**
 * 实例停止事件
 */
@Serializable
data class InstanceStopEvent(
    override val eventId: String,
    override val timestamp: Long,
    override val source: String,
    override val instanceId: String,
    val reason: String
) : InstanceEvent()
