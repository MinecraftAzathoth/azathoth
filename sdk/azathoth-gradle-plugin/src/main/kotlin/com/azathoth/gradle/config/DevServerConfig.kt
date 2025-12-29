package com.azathoth.gradle.config

import org.gradle.api.provider.Property
import org.gradle.api.provider.ListProperty
import java.io.File

/**
 * 开发服务器配置
 */
interface DevServerConfig {
    /**
     * 服务器目录
     */
    val serverDir: Property<File>

    /**
     * 是否自动复制插件
     */
    val autoCopy: Property<Boolean>

    /**
     * 是否启用热重载
     */
    val hotReload: Property<Boolean>

    /**
     * JVM 参数
     */
    val jvmArgs: ListProperty<String>
}
