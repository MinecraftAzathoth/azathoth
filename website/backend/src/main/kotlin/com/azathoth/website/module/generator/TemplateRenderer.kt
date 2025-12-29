package com.azathoth.website.module.generator

/**
 * 模板渲染器接口
 */
interface TemplateRenderer {
    /**
     * 渲染模板
     */
    fun render(template: String, context: Map<String, Any>): String

    /**
     * 注册助手函数
     */
    fun registerHelper(name: String, helper: TemplateHelper)
}

/**
 * 模板助手函数
 */
fun interface TemplateHelper {
    fun apply(args: List<Any>, context: Map<String, Any>): String
}
