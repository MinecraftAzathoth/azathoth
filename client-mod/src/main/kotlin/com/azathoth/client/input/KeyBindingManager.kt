package com.azathoth.client.input

/**
 * 按键绑定管理器
 */
interface KeyBindingManager {
    /**
     * 注册按键绑定
     */
    fun register(binding: KeyBinding)

    /**
     * 获取按键绑定
     */
    fun get(id: String): KeyBinding?

    /**
     * 检查按键是否按下
     */
    fun isPressed(id: String): Boolean
}

/**
 * 按键绑定
 */
interface KeyBinding {
    val id: String
    val name: String
    val category: String
    var keyCode: Int
    val defaultKeyCode: Int

    fun onPressed()
    fun onReleased()
}
