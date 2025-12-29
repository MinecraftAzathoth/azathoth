package com.azathoth.client.ui.component

/**
 * UI 组件基类
 */
interface UIComponent {
    val id: String
    var x: Int
    var y: Int
    var width: Int
    var height: Int
    var visible: Boolean
    var enabled: Boolean

    fun render(context: UIRenderContext)
    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean
    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean
    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean
    fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean
    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean
    fun charTyped(chr: Char, modifiers: Int): Boolean
}
