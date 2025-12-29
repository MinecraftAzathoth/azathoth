package com.azathoth.client.audio

/**
 * 音效管理器
 */
interface SoundManager {
    /**
     * 播放音效
     */
    fun play(soundId: String, volume: Float = 1.0f, pitch: Float = 1.0f)

    /**
     * 播放 3D 音效
     */
    fun playAt(soundId: String, x: Double, y: Double, z: Double, volume: Float = 1.0f, pitch: Float = 1.0f)

    /**
     * 停止音效
     */
    fun stop(soundId: String)

    /**
     * 注册自定义音效
     */
    fun register(soundId: String, resourcePath: String)
}
