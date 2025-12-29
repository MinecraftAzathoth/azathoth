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
