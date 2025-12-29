package com.azathoth.client.ui.component

/**
 * 对话框
 */
interface Dialog : UIComponent {
    var title: String
    var draggable: Boolean
    var closeable: Boolean
    var modal: Boolean
    var onClose: (() -> Unit)?

    fun show()
    fun hide()
}

/**
 * 确认对话框
 */
interface ConfirmDialog : Dialog {
    var message: String
    var confirmText: String
    var cancelText: String
    var onConfirm: (() -> Unit)?
    var onCancel: (() -> Unit)?
}
