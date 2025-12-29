package com.azathoth.client.ui.component

/**
 * 列表视图
 */
interface ListView<T> : UIComponent {
    var items: List<T>
    var selectedIndex: Int
    var itemHeight: Int
    var itemRenderer: ListItemRenderer<T>
    var onItemSelected: ((Int, T) -> Unit)?
    var onItemDoubleClicked: ((Int, T) -> Unit)?
}

/**
 * 列表项渲染器
 */
fun interface ListItemRenderer<T> {
    fun render(context: UIRenderContext, item: T, x: Int, y: Int, width: Int, height: Int, selected: Boolean, hovered: Boolean)
}
