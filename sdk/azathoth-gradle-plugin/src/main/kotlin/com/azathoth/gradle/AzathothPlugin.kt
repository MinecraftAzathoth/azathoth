package com.azathoth.gradle

import com.azathoth.gradle.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Azathoth Gradle 插件实现
 * 用于简化 Azathoth 插件开发的构建配置
 */
class AzathothPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 确保 Java 插件已应用
        project.plugins.apply(JavaPlugin::class.java)

        // 创建扩展
        val extension = project.extensions.create(
            "azathoth",
            AzathothPluginExtension::class.java
        )

        // 设置默认值
        configureDefaults(project, extension)

        // 注册任务
        registerTasks(project, extension)

        // 配置依赖
        configureAzathothDependencies(project, extension)
    }

    private fun configureDefaults(project: Project, extension: AzathothPluginExtension) {
        extension.version.convention(project.provider { project.version.toString() })
        extension.name.convention(project.provider { project.name })
        extension.apiVersion.convention("1.0")
        extension.generatePluginYml.convention(true)
        extension.shadowDependencies.convention(false)
        extension.devServer.autoCopy.convention(true)
        extension.devServer.hotReload.convention(false)
        extension.devServer.jvmArgs.convention(listOf("-Xms512m", "-Xmx2g"))
        extension.devServer.serverDir.convention(project.layout.projectDirectory.dir(".dev-server").asFile)
    }

    private fun registerTasks(project: Project, extension: AzathothPluginExtension) {
        val generateYml = project.tasks.register("generatePluginYml", GeneratePluginYmlTask::class.java) { task ->
            task.pluginId.set(extension.id)
            task.pluginVersion.set(extension.version)
            task.pluginName.set(extension.name)
            task.pluginDescription.set(extension.description)
            task.pluginAuthor.set(extension.author)
            task.mainClass.set(extension.mainClass)
            task.apiVersion.set(extension.apiVersion)
            task.pluginDependencies.set(
                extension.dependencies.map { deps -> deps.filter { !it.optional }.map { it.pluginId } }
            )
            task.pluginSoftDependencies.set(
                project.provider {
                    val soft = extension.softDependencies.getOrElse(emptyList()).toMutableList()
                    soft += extension.dependencies.getOrElse(emptyList()).filter { it.optional }.map { it.pluginId }
                    soft
                }
            )
            task.pluginProvidedServices.set(extension.providedServices)
            task.pluginConsumedServices.set(extension.consumedServices)
            task.outputDir.set(project.layout.buildDirectory.dir("generated/azathoth"))
        }

        // 如果启用了自动生成，将 generatePluginYml 的输出加入 processResources
        project.afterEvaluate {
            if (extension.generatePluginYml.getOrElse(true)) {
                project.tasks.named("processResources").configure { processResources ->
                    processResources.dependsOn(generateYml)
                    if (processResources is org.gradle.language.jvm.tasks.ProcessResources) {
                        processResources.from(generateYml.map { it.outputDir })
                    }
                }
            }
        }

        project.tasks.register("packagePlugin", PackagePluginTask::class.java) { task ->
            task.dependsOn("jar")
            task.inputDir.set(project.layout.buildDirectory.dir("libs"))
            task.outputFile.set(project.layout.buildDirectory.file(
                extension.packaging.outputFileName.orElse(
                    extension.id.map { "$it-${extension.version.get()}.jar" }
                )
            ))
            task.shadowDependencies.set(extension.shadowDependencies)
        }

        project.tasks.register("deployToDevServer", DeployToDevServerTask::class.java) { task ->
            task.dependsOn("jar")
            task.pluginJar.set(project.tasks.named("jar").map {
                (it as org.gradle.jvm.tasks.Jar).archiveFile.get()
            })
            task.serverDir.set(extension.devServer.serverDir.map { it.absolutePath })
            task.hotReload.set(extension.devServer.hotReload)
        }

        project.tasks.register("runDevServer", RunDevServerTask::class.java) { task ->
            task.serverDir.set(extension.devServer.serverDir.map { it.absolutePath })
            task.jvmArgs.set(extension.devServer.jvmArgs)
            task.debug.convention(false)
            task.debugPort.convention(5005)
        }

        project.tasks.register("validatePlugin", ValidatePluginTask::class.java) { task ->
            task.dependsOn(generateYml)
            task.pluginYml.set(generateYml.flatMap {
                it.outputDir.file("plugin.yml")
            })
        }
    }

    private fun configureAzathothDependencies(project: Project, extension: AzathothPluginExtension) {
        project.afterEvaluate {
            val compileOnly = project.configurations.findByName("compileOnly") ?: return@afterEvaluate

            // 尝试添加 azathoth-api 和 azathoth-plugin-api 作为 compileOnly 依赖
            // 如果是在 Azathoth 项目内部，使用项目依赖；否则使用 Maven 坐标
            val rootProject = project.rootProject
            val apiProject = rootProject.findProject(":sdk:azathoth-api")
            val pluginApiProject = rootProject.findProject(":sdk:azathoth-plugin-api")

            if (apiProject != null) {
                project.dependencies.add("compileOnly", project.dependencies.project(mapOf("path" to ":sdk:azathoth-api")))
            } else {
                project.dependencies.add("compileOnly", "com.azathoth:azathoth-api:${project.version}")
            }

            if (pluginApiProject != null) {
                project.dependencies.add("compileOnly", project.dependencies.project(mapOf("path" to ":sdk:azathoth-plugin-api")))
            } else {
                project.dependencies.add("compileOnly", "com.azathoth:azathoth-plugin-api:${project.version}")
            }
        }
    }
}
