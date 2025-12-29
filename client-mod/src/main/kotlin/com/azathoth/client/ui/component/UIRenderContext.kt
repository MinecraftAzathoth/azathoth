package com.azathoth.client.ui.component

/**
 * 渲染上下文（UI 专用）
 */
interface UIRenderContext {
    val screenWidth: Int
    val screenHeight: Int
    val mouseX: Int
    val mouseY: Int
    val tickDelta: Float

    // 基础绘制
    fun fill(x1: Int, y1: Int, x2: Int, y2: Int, color: Int)
    fun fillGradient(x1: Int, y1: Int, x2: Int, y2: Int, colorTop: Int, colorBottom: Int)
    fun drawBorder(x: Int, y: Int, width: Int, height: Int, color: Int)

    // 纹理绘制
    fun drawTexture(texture: String, x: Int, y: Int, u: Int, v: Int, width: Int, height: Int)
    fun drawTexture(texture: String, x: Int, y: Int, width: Int, height: Int, u: Float, v: Float, uWidth: Float, vHeight: Float)

    // 文字绘制
    fun drawText(text: String, x: Int, y: Int, color: Int, shadow: Boolean = true)
    fun drawCenteredText(text: String, centerX: Int, y: Int, color: Int)
    fun getTextWidth(text: String): Int

    // 裁剪
    fun enableScissor(x: Int, y: Int, width: Int, height: Int)
    fun disableScissor()

    // 工具提示
    fun renderTooltip(lines: List<String>, x: Int, y: Int)
}
