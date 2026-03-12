package com.azathoth.core.common.config

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

/**
 * 基于反射的配置绑定器 — 将 Configuration 绑定到 data class
 */
class ReflectionConfigurationBinder : ConfigurationBinder {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> bind(config: Configuration, clazz: KClass<T>): T {
        val constructor = clazz.primaryConstructor
            ?: throw IllegalArgumentException("${clazz.simpleName} 没有主构造函数")

        val args = mutableMapOf<KParameter, Any?>()
        for (param in constructor.parameters) {
            val key = param.name ?: continue
            val value = resolveValue(config, key, param)
            if (value != null || param.type.isMarkedNullable) {
                args[param] = value
            } else if (param.isOptional) {
                // 使用默认值，不传入参数
            } else {
                throw IllegalArgumentException("配置缺少必需的键: $key")
            }
        }
        return constructor.callBy(args)
    }

    override fun <T : Any> unbind(obj: T): Configuration {
        val map = mutableMapOf<String, Any?>()
        val clazz = obj::class
        for (prop in clazz.members) {
            if (prop is kotlin.reflect.KProperty1<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val kProp = prop as kotlin.reflect.KProperty1<T, *>
                map[prop.name] = kProp.get(obj)
            }
        }
        return MapConfiguration(map)
    }

    private fun resolveValue(config: Configuration, key: String, param: KParameter): Any? {
        val classifier = param.type.classifier as? KClass<*> ?: return config.getString(key)
        return when (classifier) {
            String::class -> config.getString(key)
            Int::class -> if (config.contains(key)) config.getInt(key) else null
            Long::class -> if (config.contains(key)) config.getLong(key) else null
            Double::class -> if (config.contains(key)) config.getDouble(key) else null
            Boolean::class -> if (config.contains(key)) config.getBoolean(key) else null
            List::class -> config.getStringList(key)
            else -> {
                // 尝试绑定子配置到嵌套 data class
                val section = config.getSection(key)
                if (section != null) bind(section, classifier) else null
            }
        }
    }
}
