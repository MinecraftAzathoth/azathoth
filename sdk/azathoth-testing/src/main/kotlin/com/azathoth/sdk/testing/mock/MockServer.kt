package com.azathoth.sdk.testing.mock

import java.util.UUID

/**
 * 模拟服务器
 */
interface MockServer {
    /** 服务器名称 */
    val name: String

    /** 是否正在运行 */
    val isRunning: Boolean

    /** 当前 Tick */
    val currentTick: Long

    /** 启动服务器 */
    suspend fun start()

    /** 停止服务器 */
    suspend fun stop()

    /** 创建模拟玩家 */
    fun createPlayer(name: String): MockPlayer

    /** 创建模拟玩家（指定ID） */
    fun createPlayer(playerId: UUID, name: String): MockPlayer

    /** 获取所有模拟玩家 */
    fun getPlayers(): Collection<MockPlayer>

    /** 移除玩家 */
    fun removePlayer(playerId: UUID)

    /** 创建模拟世界 */
    fun createWorld(name: String): MockWorld

    /** 获取世界 */
    fun getWorld(name: String): MockWorld?

    /** 获取所有世界 */
    fun getWorlds(): Collection<MockWorld>

    /** 前进指定 tick 数 */
    suspend fun advanceTicks(ticks: Int)

    /** 执行命令 */
    suspend fun executeCommand(sender: String, command: String): String

    /** 添加插件 */
    fun addPlugin(plugin: Any)
}

/**
 * 模拟玩家
 */
interface MockPlayer {
    /** 玩家UUID */
    val playerId: UUID

    /** 玩家名称 */
    val name: String

    /** 显示名称 */
    var displayName: String

    /** 是否在线 */
    val isOnline: Boolean

    /** 生命值 */
    var health: Double

    /** 最大生命值 */
    var maxHealth: Double

    /** 权限集合 */
    val permissions: MutableSet<String>

    /** 发送的消息记录 */
    val sentMessages: List<String>

    /** 收到的消息记录 */
    val receivedMessages: List<String>

    /** 执行的命令记录 */
    val executedCommands: List<String>

    /** 模拟发送聊天消息 */
    suspend fun chat(message: String)

    /** 模拟执行命令 */
    suspend fun performCommand(command: String): Boolean

    /** 模拟移动 */
    suspend fun moveTo(x: Double, y: Double, z: Double)

    /** 发送消息给玩家 */
    suspend fun sendMessage(message: String)

    /** 清除消息记录 */
    fun clearMessages()

    /** 断言收到消息 */
    fun assertReceivedMessage(message: String)

    /** 断言收到包含指定内容的消息 */
    fun assertReceivedMessageContaining(content: String)

    /** 检查权限 */
    fun hasPermission(permission: String): Boolean
}

/**
 * 模拟世界
 */
interface MockWorld {
    /** 世界名称 */
    val name: String

    /** 是否已加载 */
    val isLoaded: Boolean

    /** 设置方块 */
    suspend fun setBlock(x: Int, y: Int, z: Int, type: String)

    /** 获取方块类型 */
    fun getBlockType(x: Int, y: Int, z: Int): String

    /** 生成模拟实体 */
    suspend fun spawnMockEntity(type: String, x: Double, y: Double, z: Double): Any

    /** 清除所有实体 */
    suspend fun clearEntities()

    /** 获取实体数量 */
    fun getEntityCount(): Int
}

/**
 * 模拟服务器构建器
 */
interface MockServerBuilder {
    /** 设置服务器名称 */
    fun name(name: String): MockServerBuilder

    /** 设置 TPS */
    fun tps(tps: Int): MockServerBuilder

    /** 添加默认世界 */
    fun withDefaultWorld(name: String = "world"): MockServerBuilder

    /** 添加插件 */
    fun withPlugin(plugin: Any): MockServerBuilder

    /** 构建 */
    fun build(): MockServer
}

/**
 * 测试工具入口
 */
object MockServerFactory {
    /** 创建构建器 */
    fun builder(): MockServerBuilder = DefaultMockServerBuilder()

    /** 快速创建默认服务器 */
    fun createDefault(): MockServer = builder()
        .name("test-server")
        .tps(20)
        .withDefaultWorld()
        .build()
}
