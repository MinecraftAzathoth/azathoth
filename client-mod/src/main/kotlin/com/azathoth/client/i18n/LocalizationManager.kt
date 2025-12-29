package com.azathoth.client.i18n

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
