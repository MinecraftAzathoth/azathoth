package com.azathoth.client.ui

/**
 * 自定义 UI 管理器接口
 */
interface CustomUIManager {
    /**
     * 注册自定义 HUD 元素
     */
    fun registerHudElement(id: String, element: HudElement)

    /**
     * 注销 HUD 元素
     */
    fun unregisterHudElement(id: String)

    /**
     * 打开自定义界面
     */
    fun openScreen(screen: CustomScreen)

    /**
     * 关闭当前界面
     */
    fun closeScreen()

    /**
     * 显示通知
     */
    fun showNotification(notification: Notification)
}

/**
 * HUD 元素接口
 */
interface HudElement {
    val id: String
    var visible: Boolean
    var x: Int
    var y: Int
    val width: Int
    val height: Int

    fun render(context: RenderContext)
    fun tick()
}

/**
 * 自定义界面接口
 */
interface CustomScreen {
    val title: String
    val pausesGame: Boolean

    fun init(width: Int, height: Int)
    fun render(context: RenderContext, mouseX: Int, mouseY: Int)
    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean
    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean
    fun close()
}

/**
 * 通知
 */
interface Notification {
    val title: String
    val message: String
    val icon: String?
    val duration: Long
    val type: NotificationType
}

/**
 * 通知类型
 */
enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}
