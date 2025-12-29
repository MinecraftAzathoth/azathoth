package com.azathoth.client.ui.component

/**
 * 复选框组件
 */
interface Checkbox : UIComponent {
    var checked: Boolean
    var label: String
    var onCheckedChanged: ((Boolean) -> Unit)?
}

/**
 * 下拉选择框
 */
interface Dropdown<T> : UIComponent {
    var selectedItem: T?
    var items: List<T>
    var expanded: Boolean
    var itemRenderer: (T) -> String
    var onSelectionChanged: ((T?) -> Unit)?
}

/**
 * 颜色选择器
 */
interface ColorPicker : UIComponent {
    var color: Int
    var showAlpha: Boolean
    var onColorChanged: ((Int) -> Unit)?
}
