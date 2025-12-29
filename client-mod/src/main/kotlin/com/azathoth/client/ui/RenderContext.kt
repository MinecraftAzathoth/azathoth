package com.azathoth.client.ui

/**
 * 渲染上下文
 */
interface RenderContext {
    val screenWidth: Int
    val screenHeight: Int
    val tickDelta: Float

    fun drawTexture(texture: String, x: Int, y: Int, width: Int, height: Int)
    fun drawText(text: String, x: Int, y: Int, color: Int)
    fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int)
    fun drawGradient(x: Int, y: Int, width: Int, height: Int, colorStart: Int, colorEnd: Int)
}
