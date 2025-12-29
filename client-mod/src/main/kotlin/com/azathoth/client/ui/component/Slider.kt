package com.azathoth.client.ui.component

/**
 * 滑块组件
 */
interface Slider : UIComponent {
    var value: Double
    var minValue: Double
    var maxValue: Double
    var step: Double
    var onValueChanged: ((Double) -> Unit)?
}

/**
 * 进度条
 */
interface ProgressBar : UIComponent {
    var progress: Double
    var maxProgress: Double
    var showText: Boolean
    var textFormat: (Double, Double) -> String
    var foregroundColor: Int
    var backgroundColor: Int
}
