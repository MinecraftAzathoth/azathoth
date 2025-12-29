package com.azathoth.client.ui

/**
 * 自定义 UI 组件基类
 */
interface UIComponent {
    val id: String
    var x: Int
    var y: Int
    var width: Int
    var height: Int
    var visible: Boolean
    var enabled: Boolean

    fun render(context: RenderContext)
    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean
    fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean
    fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean
    fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean
    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean
    fun charTyped(chr: Char, modifiers: Int): Boolean
}

/**
 * 渲染上下文（UI 专用）
 */
interface RenderContext {
    val screenWidth: Int
    val screenHeight: Int
    val mouseX: Int
    val mouseY: Int
    val tickDelta: Float

    // 基础绘制
    fun fill(x1: Int, y1: Int, x2: Int, y2: Int, color: Int)
    fun fillGradient(x1: Int, y1: Int, x2: Int, y2: Int, colorTop: Int, colorBottom: Int)
    fun drawBorder(x: Int, y: Int, width: Int, height: Int, color: Int)

    // 纹理绘制
    fun drawTexture(texture: String, x: Int, y: Int, u: Int, v: Int, width: Int, height: Int)
    fun drawTexture(texture: String, x: Int, y: Int, width: Int, height: Int, u: Float, v: Float, uWidth: Float, vHeight: Float)

    // 文字绘制
    fun drawText(text: String, x: Int, y: Int, color: Int, shadow: Boolean = true)
    fun drawCenteredText(text: String, centerX: Int, y: Int, color: Int)
    fun getTextWidth(text: String): Int

    // 裁剪
    fun enableScissor(x: Int, y: Int, width: Int, height: Int)
    fun disableScissor()

    // 工具提示
    fun renderTooltip(lines: List<String>, x: Int, y: Int)
}

/**
 * 按钮组件
 */
interface Button : UIComponent {
    var text: String
    var onClick: (() -> Unit)?
    var onHover: ((Boolean) -> Unit)?
}

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
    fun render(context: RenderContext, item: T, x: Int, y: Int, width: Int, height: Int, selected: Boolean, hovered: Boolean)
}

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

/**
 * 颜色选择器
 */
interface ColorPicker : UIComponent {
    var color: Int
    var showAlpha: Boolean
    var onColorChanged: ((Int) -> Unit)?
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
