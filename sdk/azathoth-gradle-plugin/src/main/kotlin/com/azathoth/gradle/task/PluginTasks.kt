package com.azathoth.gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

/**
 * 生成 plugin.yml 任务
 */
abstract class GeneratePluginYmlTask : DefaultTask() {

    @get:Input
    abstract val pluginId: Property<String>

    @get:Input
    abstract val pluginVersion: Property<String>

    @get:Input
    abstract val pluginName: Property<String>

    @get:Input
    @get:Optional
    abstract val pluginDescription: Property<String>

    @get:Input
    @get:Optional
    abstract val pluginAuthor: Property<String>

    @get:Input
    abstract val mainClass: Property<String>

    @get:Input
    abstract val apiVersion: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        // 由具体实现类完成
    }
}

/**
 * 打包插件任务
 */
abstract class PackagePluginTask : DefaultTask() {

    @get:InputFiles
    abstract val inputFiles: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val shadowDependencies: Property<Boolean>

    @TaskAction
    fun packagePlugin() {
        // 由具体实现类完成
    }
}

/**
 * 部署到开发服务器任务
 */
abstract class DeployToDevServerTask : DefaultTask() {

    @get:InputFile
    abstract val pluginJar: RegularFileProperty

    @get:Input
    abstract val serverDir: Property<File>

    @get:Input
    abstract val hotReload: Property<Boolean>

    @TaskAction
    fun deploy() {
        // 由具体实现类完成
    }
}

/**
 * 发布到市场任务
 */
abstract class PublishToMarketplaceTask : DefaultTask() {

    @get:InputFile
    abstract val pluginJar: RegularFileProperty

    @get:Input
    abstract val apiKey: Property<String>

    @get:Input
    @get:Optional
    abstract val changelog: Property<String>

    @get:Input
    abstract val releaseType: Property<String>

    @TaskAction
    fun publish() {
        // 由具体实现类完成
    }
}

/**
 * 验证插件配置任务
 */
abstract class ValidatePluginTask : DefaultTask() {

    @get:InputFile
    abstract val pluginYml: RegularFileProperty

    @get:InputFile
    abstract val pluginJar: RegularFileProperty

    @TaskAction
    fun validate() {
        // 由具体实现类完成
    }
}

/**
 * 运行开发服务器任务
 */
abstract class RunDevServerTask : DefaultTask() {

    @get:Input
    abstract val serverDir: Property<File>

    @get:Input
    abstract val jvmArgs: Property<List<String>>

    @get:Input
    abstract val debug: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val debugPort: Property<Int>

    @TaskAction
    fun run() {
        // 由具体实现类完成
    }
}
