package com.azathoth.client.particle

import com.azathoth.client.ui.RenderContext

/**
 * 粒子效果管理器
 */
interface ParticleManager {
    /**
     * 生成粒子效果
     */
    fun spawn(particleId: String, x: Double, y: Double, z: Double, count: Int = 1)

    /**
     * 生成自定义粒子效果
     */
    fun spawnCustom(effect: ParticleEffect)

    /**
     * 注册自定义粒子
     */
    fun register(particleId: String, factory: ParticleFactory)
}

/**
 * 粒子效果
 */
interface ParticleEffect {
    val particleId: String
    val x: Double
    val y: Double
    val z: Double
    val velocityX: Double
    val velocityY: Double
    val velocityZ: Double
    val count: Int
    val spread: Double
}

/**
 * 粒子工厂
 */
fun interface ParticleFactory {
    fun create(x: Double, y: Double, z: Double, vx: Double, vy: Double, vz: Double): Particle
}

/**
 * 粒子
 */
interface Particle {
    var x: Double
    var y: Double
    var z: Double
    var age: Int
    val maxAge: Int
    val isAlive: Boolean

    fun tick()
    fun render(context: RenderContext)
}
