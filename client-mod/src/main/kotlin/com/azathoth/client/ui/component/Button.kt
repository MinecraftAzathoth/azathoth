package com.azathoth.client.ui.component

/**
 * 按钮组件
 */
interface Button : UIComponent {
    var text: String
    var onClick: (() -> Unit)?
    var onHover: ((Boolean) -> Unit)?
}

/**
 * 图标按钮
 */
interface IconButton : UIComponent {
    var icon: String
    var iconSize: Int
    var tooltip: String?
    var onClick: (() -> Unit)?
}
