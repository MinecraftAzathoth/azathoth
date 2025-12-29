package com.azathoth.client.config

/**
 * 客户端配置接口
 */
interface ClientConfig {
    /**
     * 服务器地址
     */
    var serverAddress: String

    /**
     * 服务器端口
     */
    var serverPort: Int

    /**
     * 是否启用自定义 UI
     */
    var enableCustomUI: Boolean

    /**
     * 是否启用粒子增强
     */
    var enableParticleEffects: Boolean

    /**
     * 是否启用音效增强
     */
    var enableSoundEffects: Boolean

    /**
     * UI 缩放比例
     */
    var uiScale: Float

    /**
     * 保存配置
     */
    fun save()

    /**
     * 加载配置
     */
    fun load()
}
