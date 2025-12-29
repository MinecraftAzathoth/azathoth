package com.azathoth.core.common.lifecycle

/**
 * 生命周期状态
 */
enum class LifecycleState {
    CREATED,
    INITIALIZING,
    INITIALIZED,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    FAILED
}

/**
 * 生命周期管理接口
 */
interface Lifecycle {
    /** 当前生命周期状态 */
    val state: LifecycleState
    
    /** 是否正在运行 */
    val isRunning: Boolean get() = state == LifecycleState.RUNNING
    
    /** 初始化 */
    suspend fun initialize()
    
    /** 启动 */
    suspend fun start()
    
    /** 停止 */
    suspend fun stop()
}

/**
 * 可重载的生命周期
 */
interface ReloadableLifecycle : Lifecycle {
    /** 重载配置/状态 */
    suspend fun reload()
}

/**
 * 生命周期监听器
 */
interface LifecycleListener {
    /** 状态变更时调用 */
    suspend fun onStateChange(oldState: LifecycleState, newState: LifecycleState)
}

/**
 * 支持监听器的生命周期
 */
interface ObservableLifecycle : Lifecycle {
    /** 添加监听器 */
    fun addListener(listener: LifecycleListener)
    
    /** 移除监听器 */
    fun removeListener(listener: LifecycleListener)
}
