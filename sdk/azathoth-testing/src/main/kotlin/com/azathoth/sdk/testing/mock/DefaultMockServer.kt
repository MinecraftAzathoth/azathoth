package com.azathoth.sdk.testing.mock

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

private val logger = KotlinLogging.logger {}

/**
 * 模拟服务器默认实现
 */
class DefaultMockServer(
    override val name: String = "test-server",
    private val tps: Int = 20,
    private val defaultWorldName: String? = null,
    private val initialPlugins: List<Any> = emptyList()
) : MockServer {

    override var isRunning: Boolean = false
        private set

    private val tickCounter = AtomicLong(0)
    override val currentTick: Long get() = tickCounter.get()

    private val players = ConcurrentHashMap<UUID, MockPlayer>()
    private val worlds = ConcurrentHashMap<String, MockWorld>()
    private val plugins = CopyOnWriteArrayList<Any>()
    private val commandHandlers = ConcurrentHashMap<String, (String, String) -> String>()

    override suspend fun start() {
        isRunning = true
        defaultWorldName?.let { createWorld(it) }
        initialPlugins.forEach { plugins.add(it) }
        logger.info { "模拟服务器 '$name' 已启动 (TPS=$tps)" }
    }

    override suspend fun stop() {
        isRunning = false
        players.clear()
        logger.info { "模拟服务器 '$name' 已停止" }
    }

    override fun createPlayer(name: String): MockPlayer =
        createPlayer(UUID.randomUUID(), name)

    override fun createPlayer(playerId: UUID, name: String): MockPlayer {
        val player = DefaultMockPlayer(playerId, name)
        players[playerId] = player
        return player
    }

    override fun getPlayers(): Collection<MockPlayer> = players.values

    override fun removePlayer(playerId: UUID) {
        players.remove(playerId)
    }

    override fun createWorld(name: String): MockWorld {
        val world = DefaultMockWorld(name)
        worlds[name] = world
        return world
    }

    override fun getWorld(name: String): MockWorld? = worlds[name]

    override fun getWorlds(): Collection<MockWorld> = worlds.values

    override suspend fun advanceTicks(ticks: Int) {
        repeat(ticks) {
            tickCounter.incrementAndGet()
        }
    }

    override suspend fun executeCommand(sender: String, command: String): String {
        val handler = commandHandlers[command.split(" ").first()]
        return handler?.invoke(sender, command) ?: "Unknown command: $command"
    }

    override fun addPlugin(plugin: Any) {
        plugins.add(plugin)
    }
}

/**
 * 模拟玩家默认实现
 */
class DefaultMockPlayer(
    override val playerId: UUID,
    override val name: String
) : MockPlayer {

    override var displayName: String = name
    override var isOnline: Boolean = true
    override var health: Double = 20.0
    override var maxHealth: Double = 20.0
    override val permissions: MutableSet<String> = mutableSetOf()

    private val _sentMessages = CopyOnWriteArrayList<String>()
    private val _receivedMessages = CopyOnWriteArrayList<String>()
    private val _executedCommands = CopyOnWriteArrayList<String>()

    override val sentMessages: List<String> get() = _sentMessages.toList()
    override val receivedMessages: List<String> get() = _receivedMessages.toList()
    override val executedCommands: List<String> get() = _executedCommands.toList()

    var x: Double = 0.0; private set
    var y: Double = 64.0; private set
    var z: Double = 0.0; private set

    override suspend fun chat(message: String) {
        _sentMessages.add(message)
    }

    override suspend fun performCommand(command: String): Boolean {
        _executedCommands.add(command)
        return true
    }

    override suspend fun moveTo(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    override suspend fun sendMessage(message: String) {
        _receivedMessages.add(message)
    }

    override fun clearMessages() {
        _sentMessages.clear()
        _receivedMessages.clear()
        _executedCommands.clear()
    }

    override fun assertReceivedMessage(message: String) {
        if (message !in _receivedMessages) {
            throw AssertionError(
                "期望收到消息 '$message'，但实际收到: $_receivedMessages"
            )
        }
    }

    override fun assertReceivedMessageContaining(content: String) {
        if (_receivedMessages.none { content in it }) {
            throw AssertionError(
                "期望收到包含 '$content' 的消息，但实际收到: $_receivedMessages"
            )
        }
    }

    override fun hasPermission(permission: String): Boolean = permission in permissions
}

/**
 * 模拟世界默认实现
 */
class DefaultMockWorld(
    override val name: String
) : MockWorld {

    override var isLoaded: Boolean = true
        private set

    // key: "x,y,z" -> block type
    private val blocks = ConcurrentHashMap<String, String>()
    private val entities = CopyOnWriteArrayList<MockEntity>()

    override suspend fun setBlock(x: Int, y: Int, z: Int, type: String) {
        blocks["$x,$y,$z"] = type
    }

    override fun getBlockType(x: Int, y: Int, z: Int): String =
        blocks["$x,$y,$z"] ?: "air"

    override suspend fun spawnMockEntity(type: String, x: Double, y: Double, z: Double): Any {
        val entity = MockEntity(UUID.randomUUID(), type, x, y, z)
        entities.add(entity)
        return entity
    }

    override suspend fun clearEntities() {
        entities.clear()
    }

    override fun getEntityCount(): Int = entities.size
}

/**
 * 模拟实体
 */
data class MockEntity(
    val uuid: UUID,
    val type: String,
    val x: Double,
    val y: Double,
    val z: Double
)

/**
 * 默认 MockServerBuilder 实现
 */
class DefaultMockServerBuilder : MockServerBuilder {
    private var name: String = "test-server"
    private var tps: Int = 20
    private var defaultWorldName: String? = null
    private val plugins = mutableListOf<Any>()

    override fun name(name: String) = apply { this.name = name }
    override fun tps(tps: Int) = apply { this.tps = tps }
    override fun withDefaultWorld(name: String) = apply { this.defaultWorldName = name }
    override fun withPlugin(plugin: Any) = apply { this.plugins.add(plugin) }

    override fun build(): MockServer = DefaultMockServer(
        name = name,
        tps = tps,
        defaultWorldName = defaultWorldName,
        initialPlugins = plugins.toList()
    )
}
