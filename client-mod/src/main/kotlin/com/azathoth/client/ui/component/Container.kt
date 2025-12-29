package com.azathoth.client.ui.component

/**
 * 滚动面板
 */
interface ScrollPanel : UIComponent {
    var scrollX: Int
    var scrollY: Int
    var contentWidth: Int
    var contentHeight: Int
    var showHorizontalScrollbar: Boolean
    var showVerticalScrollbar: Boolean

    fun addChild(component: UIComponent)
    fun removeChild(component: UIComponent)
    fun clearChildren()
}

/**
 * 标签页容器
 */
interface TabContainer : UIComponent {
    var selectedTabIndex: Int
    val tabs: List<Tab>

    fun addTab(tab: Tab)
    fun removeTab(index: Int)
    fun selectTab(index: Int)
}

/**
 * 标签页
 */
interface Tab {
    val title: String
    val icon: String?
    val content: UIComponent
}
