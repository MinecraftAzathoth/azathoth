---
title: '快速开始'
description: '在几分钟内创建您的第一个 Azathoth 项目'
navigation:
  order: 1
---

# 快速开始

本指南将帮助您快速创建第一个 Azathoth 项目，并了解框架的基本概念。

## 前置要求

在开始之前，请确保您的开发环境满足以下要求：

- **JDK 21** 或更高版本
- **Gradle 8.0** 或更高版本
- **Minecraft 服务端** (Paper/Spigot 1.20+)
- **IDE**: 推荐使用 IntelliJ IDEA

## 创建项目

### 使用项目生成器（推荐）

最快的方式是使用我们的在线项目生成器：

1. 访问 [Azathoth Generator](/generator)
2. 填写项目信息
3. 选择需要的功能模块
4. 下载生成的项目

### 手动配置

如果您想手动配置项目，请按以下步骤操作：

**1. 创建 `build.gradle.kts`**

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    id("com.azathoth.gradle") version "1.0.0"
}

azathoth {
    pluginId = "my-plugin"
    pluginName = "My First Plugin"
    version = "1.0.0"

    // 可选：启用的功能模块
    features {
        skillSystem = true
        questSystem = true
    }
}

dependencies {
    implementation("com.azathoth:azathoth-api:1.0.0")
}
```

**2. 创建插件主类**

```kotlin
package com.example.myplugin

import com.azathoth.api.plugin.AzathothPlugin
import com.azathoth.api.plugin.AzathothPluginBase

@AzathothPlugin(
    id = "my-plugin",
    name = "My First Plugin",
    version = "1.0.0",
    authors = ["YourName"]
)
class MyPlugin : AzathothPluginBase() {

    override fun onEnable() {
        logger.info { "My plugin is enabled!" }
    }

    override fun onDisable() {
        logger.info { "My plugin is disabled!" }
    }
}
```

## 项目结构

创建完成后，您的项目结构应该类似：

```
my-plugin/
├── build.gradle.kts
├── settings.gradle.kts
└── src/
    └── main/
        ├── kotlin/
        │   └── com/example/myplugin/
        │       └── MyPlugin.kt
        └── resources/
            └── plugin.yml
```

## 构建和运行

### 构建项目

```bash
./gradlew build
```

构建成功后，JAR 文件将位于 `build/libs/` 目录。

### 部署到服务器

1. 将生成的 JAR 文件复制到服务器的 `plugins` 目录
2. 确保 Azathoth Core 已安装
3. 重启服务器

### 验证安装

在服务器控制台输入：

```
/azathoth plugins
```

您应该能看到您的插件已成功加载。

## 下一步

- [安装指南](/docs/installation) - 详细的环境配置说明
- [项目结构](/docs/project-structure) - 深入了解项目组织方式
- [架构设计](/docs/core/architecture) - 理解 Azathoth 的核心架构
