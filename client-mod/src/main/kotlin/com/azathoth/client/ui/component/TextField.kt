package com.azathoth.client.ui.component

/**
 * 文本输入框
 */
interface TextField : UIComponent {
    var text: String
    var placeholder: String
    var maxLength: Int
    var editable: Boolean
    var focused: Boolean
    var onTextChanged: ((String) -> Unit)?
    var onEnterPressed: (() -> Unit)?
}
