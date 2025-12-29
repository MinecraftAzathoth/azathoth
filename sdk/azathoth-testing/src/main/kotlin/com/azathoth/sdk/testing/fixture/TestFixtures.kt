package com.azathoth.sdk.testing.fixture

import com.azathoth.core.common.identity.PlayerId
import java.util.UUID

/**
 * 测试数据构建器基类
 */
abstract class FixtureBuilder<T> {
    abstract fun build(): T
}

/**
 * 玩家数据夹具
 */
data class PlayerFixture(
    val playerId: PlayerId,
    val username: String,
    val displayName: String,
    val level: Int,
    val experience: Long,
    val gold: Long,
    val diamond: Long,
    val vipLevel: Int
) {
    class Builder : FixtureBuilder<PlayerFixture>() {
        private var playerId: PlayerId = PlayerId.of(UUID.randomUUID())
        private var username: String = "TestPlayer"
        private var displayName: String = "TestPlayer"
        private var level: Int = 1
        private var experience: Long = 0
        private var gold: Long = 0
        private var diamond: Long = 0
        private var vipLevel: Int = 0
        
        fun playerId(id: PlayerId) = apply { this.playerId = id }
        fun username(name: String) = apply { this.username = name; this.displayName = name }
        fun displayName(name: String) = apply { this.displayName = name }
        fun level(level: Int) = apply { this.level = level }
        fun experience(exp: Long) = apply { this.experience = exp }
        fun gold(amount: Long) = apply { this.gold = amount }
        fun diamond(amount: Long) = apply { this.diamond = amount }
        fun vipLevel(level: Int) = apply { this.vipLevel = level }
        
        override fun build() = PlayerFixture(
            playerId, username, displayName, level, experience, gold, diamond, vipLevel
        )
    }
    
    companion object {
        fun builder() = Builder()
        
        fun default() = builder().build()
        
        fun withLevel(level: Int) = builder().level(level).build()
        
        fun rich() = builder().gold(1000000).diamond(10000).build()
    }
}

/**
 * 物品数据夹具
 */
data class ItemFixture(
    val itemId: String,
    val amount: Int,
    val displayName: String?,
    val lore: List<String>,
    val data: Map<String, Any>
) {
    class Builder : FixtureBuilder<ItemFixture>() {
        private var itemId: String = "test:item"
        private var amount: Int = 1
        private var displayName: String? = null
        private var lore: List<String> = emptyList()
        private var data: Map<String, Any> = emptyMap()
        
        fun itemId(id: String) = apply { this.itemId = id }
        fun amount(amount: Int) = apply { this.amount = amount }
        fun displayName(name: String) = apply { this.displayName = name }
        fun lore(vararg lines: String) = apply { this.lore = lines.toList() }
        fun data(data: Map<String, Any>) = apply { this.data = data }
        
        override fun build() = ItemFixture(itemId, amount, displayName, lore, data)
    }
    
    companion object {
        fun builder() = Builder()
        
        fun default() = builder().build()
        
        fun stackOf(id: String, amount: Int) = builder().itemId(id).amount(amount).build()
    }
}

/**
 * 位置数据夹具
 */
data class LocationFixture(
    val world: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float
) {
    class Builder : FixtureBuilder<LocationFixture>() {
        private var world: String = "world"
        private var x: Double = 0.0
        private var y: Double = 64.0
        private var z: Double = 0.0
        private var yaw: Float = 0f
        private var pitch: Float = 0f
        
        fun world(world: String) = apply { this.world = world }
        fun x(x: Double) = apply { this.x = x }
        fun y(y: Double) = apply { this.y = y }
        fun z(z: Double) = apply { this.z = z }
        fun position(x: Double, y: Double, z: Double) = apply { 
            this.x = x; this.y = y; this.z = z 
        }
        fun yaw(yaw: Float) = apply { this.yaw = yaw }
        fun pitch(pitch: Float) = apply { this.pitch = pitch }
        fun rotation(yaw: Float, pitch: Float) = apply { 
            this.yaw = yaw; this.pitch = pitch 
        }
        
        override fun build() = LocationFixture(world, x, y, z, yaw, pitch)
    }
    
    companion object {
        fun builder() = Builder()
        
        fun origin() = builder().build()
        
        fun at(x: Double, y: Double, z: Double) = builder().position(x, y, z).build()
        
        fun random() = builder()
            .position(
                Math.random() * 1000 - 500,
                Math.random() * 100 + 64,
                Math.random() * 1000 - 500
            )
            .build()
    }
}

/**
 * 测试断言工具
 */
object TestAssertions {
    /**
     * 断言在指定时间内条件成立
     */
    suspend fun assertEventually(
        timeoutMs: Long = 5000,
        intervalMs: Long = 100,
        condition: suspend () -> Boolean
    ) {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (condition()) return
            kotlinx.coroutines.delay(intervalMs)
        }
        throw AssertionError("Condition not met within ${timeoutMs}ms")
    }
    
    /**
     * 断言抛出指定异常
     */
    inline fun <reified T : Throwable> assertThrows(block: () -> Unit): T {
        try {
            block()
            throw AssertionError("Expected ${T::class.simpleName} but nothing was thrown")
        } catch (e: Throwable) {
            if (e is T) return e
            throw AssertionError("Expected ${T::class.simpleName} but got ${e::class.simpleName}", e)
        }
    }
}
