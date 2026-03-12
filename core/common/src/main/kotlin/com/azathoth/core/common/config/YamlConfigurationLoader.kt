package com.azathoth.core.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.nio.file.Files
import java.nio.file.Path

/**
 * YAML 配置加载器
 */
class YamlConfigurationLoader : ConfigurationLoader<MapConfiguration> {

    private val yaml = ObjectMapper(YAMLFactory())

    @Suppress("UNCHECKED_CAST")
    override suspend fun load(path: String): MapConfiguration {
        val file = Path.of(path)
        if (!Files.exists(file)) {
            return MapConfiguration()
        }
        val data = Files.newInputStream(file).use { stream ->
            yaml.readValue(stream, Map::class.java) as? Map<String, Any?> ?: emptyMap()
        }
        return MapConfiguration(data.toMutableMap())
    }

    override suspend fun save(config: MapConfiguration, path: String) {
        val file = Path.of(path)
        Files.createDirectories(file.parent)
        Files.newOutputStream(file).use { stream ->
            yaml.writeValue(stream, config.toMap())
        }
    }
}
