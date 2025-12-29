package com.azathoth.sdk.plugin.annotation

import com.azathoth.sdk.plugin.loader.LoadOrder

/**
 * 插件信息注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Plugin(
    /** 插件ID */
    val id: String,
    /** 插件名称 */
    val name: String,
    /** 版本 */
    val version: String,
    /** 描述 */
    val description: String = "",
    /** 作者 */
    val authors: Array<String> = [],
    /** 网站 */
    val website: String = "",
    /** API 版本 */
    val apiVersion: String = "1.0",
    /** 加载顺序 */
    val loadOrder: LoadOrder = LoadOrder.POSTWORLD
)

/**
 * 依赖注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class Dependency(
    /** 插件ID */
    val id: String,
    /** 最小版本 */
    val minVersion: String = "",
    /** 最大版本 */
    val maxVersion: String = "",
    /** 是否软依赖 */
    val soft: Boolean = false
)

/**
 * 配置文件注解
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigFile(
    /** 配置文件名 */
    val value: String = "config.yml"
)

/**
 * 自动注入服务
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Inject

/**
 * 生命周期方法标记
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnLoad

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnEnable

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnDisable

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnReload

/**
 * 定时任务注解
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Scheduled(
    /** 延迟（tick） */
    val delay: Long = 0,
    /** 周期（tick），0表示只执行一次 */
    val period: Long = 0,
    /** 是否异步 */
    val async: Boolean = false
)

/**
 * 权限注册注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class RegisterPermission(
    /** 权限节点 */
    val node: String,
    /** 描述 */
    val description: String = "",
    /** 默认值 */
    val default: String = "op"
)
