package com.azathoth.client

/**
 * Azathoth 客户端模组主入口接口
 * 基于 Fabric 模组系统
 */
interface AzathothClientMod {
    /**
     * 模组初始化
     */
    fun onInitialize()

    /**
     * 客户端初始化
     */
    fun onInitializeClient()
}

/**
 * 客户端配置接口
 */
interface ClientConfig {
    /**
     * 服务器地址
     */
    var serverAddress: String

    /**
     * 服务器端口
     */
    var serverPort: Int

    /**
     * 是否启用自定义 UI
     */
    var enableCustomUI: Boolean

    /**
     * 是否启用粒子增强
     */
    var enableParticleEffects: Boolean

    /**
     * 是否启用音效增强
     */
    var enableSoundEffects: Boolean

    /**
     * UI 缩放比例
     */
    var uiScale: Float

    /**
     * 保存配置
     */
    fun save()

    /**
     * 加载配置
     */
    fun load()
}

/**
 * 网络通信接口
 */
interface ClientNetworkHandler {
    /**
     * 连接状态
     */
    val isConnected: Boolean

    /**
     * 发送自定义数据包
     */
    fun sendPacket(channel: String, data: ByteArray)

    /**
     * 注册数据包处理器
     */
    fun registerHandler(channel: String, handler: PacketHandler)

    /**
     * 注销数据包处理器
     */
    fun unregisterHandler(channel: String)
}

/**
 * 数据包处理器
 */
fun interface PacketHandler {
    fun handle(data: ByteArray)
}

/**
 * 自定义 UI 管理器接口
 */
interface CustomUIManager {
    /**
     * 注册自定义 HUD 元素
     */
    fun registerHudElement(id: String, element: HudElement)

    /**
     * 注销 HUD 元素
     */
    fun unregisterHudElement(id: String)

    /**
     * 打开自定义界面
     */
    fun openScreen(screen: CustomScreen)

    /**
     * 关闭当前界面
     */
    fun closeScreen()

    /**
     * 显示通知
     */
    fun showNotification(notification: Notification)
}

/**
 * HUD 元素接口
 */
interface HudElement {
    /**
     * 元素 ID
     */
    val id: String

    /**
     * 是否可见
     */
    var visible: Boolean

    /**
     * X 坐标
     */
    var x: Int

    /**
     * Y 坐标
     */
    var y: Int

    /**
     * 宽度
     */
    val width: Int

    /**
     * 高度
     */
    val height: Int

    /**
     * 渲染
     */
    fun render(context: RenderContext)

    /**
     * 更新
     */
    fun tick()
}

/**
 * 渲染上下文
 */
interface RenderContext {
    val screenWidth: Int
    val screenHeight: Int
    val tickDelta: Float

    fun drawTexture(texture: String, x: Int, y: Int, width: Int, height: Int)
    fun drawText(text: String, x: Int, y: Int, color: Int)
    fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int)
    fun drawGradient(x: Int, y: Int, width: Int, height: Int, colorStart: Int, colorEnd: Int)
}

/**
 * 自定义界面接口
 */
interface CustomScreen {
    /**
     * 界面标题
     */
    val title: String

    /**
     * 是否暂停游戏
     */
    val pausesGame: Boolean

    /**
     * 初始化
     */
    fun init(width: Int, height: Int)

    /**
     * 渲染
     */
    fun render(context: RenderContext, mouseX: Int, mouseY: Int)

    /**
     * 鼠标点击
     */
    fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean

    /**
     * 键盘输入
     */
    fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean

    /**
     * 关闭
     */
    fun close()
}

/**
 * 通知
 */
interface Notification {
    val title: String
    val message: String
    val icon: String?
    val duration: Long
    val type: NotificationType
}

/**
 * 通知类型
 */
enum class NotificationType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR
}

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

/**
 * 音效管理器
 */
interface SoundManager {
    /**
     * 播放音效
     */
    fun play(soundId: String, volume: Float = 1.0f, pitch: Float = 1.0f)

    /**
     * 播放 3D 音效
     */
    fun playAt(soundId: String, x: Double, y: Double, z: Double, volume: Float = 1.0f, pitch: Float = 1.0f)

    /**
     * 停止音效
     */
    fun stop(soundId: String)

    /**
     * 注册自定义音效
     */
    fun register(soundId: String, resourcePath: String)
}

/**
 * 粒子效果管理器
 */
interface ParticleManager {
    /**
     * 生成粒子效果
     */
    fun spawn(particleId: String, x: Double, y: Double, z: Double, count: Int = 1)

    /**
     * 生成自定义粒子效果
     */
    fun spawnCustom(effect: ParticleEffect)

    /**
     * 注册自定义粒子
     */
    fun register(particleId: String, factory: ParticleFactory)
}

/**
 * 粒子效果
 */
interface ParticleEffect {
    val particleId: String
    val x: Double
    val y: Double
    val z: Double
    val velocityX: Double
    val velocityY: Double
    val velocityZ: Double
    val count: Int
    val spread: Double
}

/**
 * 粒子工厂
 */
fun interface ParticleFactory {
    fun create(x: Double, y: Double, z: Double, vx: Double, vy: Double, vz: Double): Particle
}

/**
 * 粒子
 */
interface Particle {
    var x: Double
    var y: Double
    var z: Double
    var age: Int
    val maxAge: Int
    val isAlive: Boolean

    fun tick()
    fun render(context: RenderContext)
}

/**
 * 本地化管理器
 */
interface LocalizationManager {
    /**
     * 当前语言
     */
    var currentLanguage: String

    /**
     * 获取翻译文本
     */
    fun translate(key: String, vararg args: Any): String

    /**
     * 检查是否有翻译
     */
    fun hasTranslation(key: String): Boolean

    /**
     * 加载语言文件
     */
    fun loadLanguage(language: String, resourcePath: String)
}
