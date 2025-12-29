---
title: 项目生成器 API
description: 项目脚手架生成相关接口
navigation:
  title: 生成器模块
  order: 5
---

# 项目生成器模块 API

## 数据模型

### ProjectType (项目类型)

| 值 | 描述 |
|----|------|
| `GAME_PLUGIN` | 游戏内容插件 |
| `EXTENSION_PLUGIN` | 扩展服务插件 |
| `ADMIN_MODULE` | 管理后台模块 |

### FeatureModule (可选模块)

| 值 | 描述 |
|----|------|
| `SKILL_SYSTEM` | 技能系统 |
| `DUNGEON_SYSTEM` | 副本系统 |
| `AI_BEHAVIOR` | AI 行为 |
| `ITEM_SYSTEM` | 物品系统 |
| `QUEST_SYSTEM` | 任务系统 |
| `COMMAND_SYSTEM` | 命令系统 |
| `DATABASE` | 数据库集成 |
| `REDIS` | Redis 集成 |
| `GRPC_CLIENT` | gRPC 客户端 |

### ProjectConfig (项目配置)

| 字段 | 类型 | 描述 |
|------|------|------|
| projectName | string | 项目名称 |
| groupId | string | 组织ID (如 com.example) |
| version | string | 初始版本号 |
| description | string | 项目描述 |
| author | string | 作者名称 |
| projectType | ProjectType | 项目类型 |
| features | FeatureModule[] | 选择的功能模块 |
| kotlinVersion | string | Kotlin 版本 |
| javaVersion | int | Java 版本 |
| includeExamples | boolean | 是否包含示例代码 |
| includeTests | boolean | 是否包含测试 |

### TemplateInfo (模板信息)

| 字段 | 类型 | 描述 |
|------|------|------|
| templateId | string | 模板ID |
| name | string | 模板名称 |
| description | string | 模板描述 |
| projectType | ProjectType | 项目类型 |
| defaultFeatures | FeatureModule[] | 默认功能模块 |
| preview | string | 预览图URL |

### GeneratedFile (生成的文件)

| 字段 | 类型 | 描述 |
|------|------|------|
| path | string | 文件路径 |
| content | string | 文件内容 |
| isDirectory | boolean | 是否是目录 |

### GenerationResult (生成结果)

| 字段 | 类型 | 描述 |
|------|------|------|
| success | boolean | 是否成功 |
| files | GeneratedFile[] | 生成的文件列表 |
| downloadUrl | string? | 下载链接 |
| error | string? | 错误信息 |

### ValidationResult (验证结果)

| 字段 | 类型 | 描述 |
|------|------|------|
| valid | boolean | 是否有效 |
| errors | ValidationError[] | 错误列表 |
| warnings | string[] | 警告列表 |

### ValidationError (验证错误)

| 字段 | 类型 | 描述 |
|------|------|------|
| field | string | 字段名 |
| message | string | 错误信息 |

---

## 获取可用模板

```
GET /generator/templates
```

### 响应

```json
{
  "success": true,
  "templates": [
    {
      "templateId": "game_plugin_basic",
      "name": "游戏插件 - 基础模板",
      "description": "适合入门开发者的基础游戏插件模板",
      "projectType": "GAME_PLUGIN",
      "defaultFeatures": ["COMMAND_SYSTEM"],
      "preview": "https://..."
    },
    {
      "templateId": "game_plugin_full",
      "name": "游戏插件 - 完整模板",
      "description": "包含所有游戏系统的完整模板",
      "projectType": "GAME_PLUGIN",
      "defaultFeatures": ["SKILL_SYSTEM", "ITEM_SYSTEM", "QUEST_SYSTEM", "DUNGEON_SYSTEM"],
      "preview": "https://..."
    },
    {
      "templateId": "extension_plugin",
      "name": "扩展服务插件",
      "description": "用于开发扩展服务的模板",
      "projectType": "EXTENSION_PLUGIN",
      "defaultFeatures": ["DATABASE", "GRPC_CLIENT"],
      "preview": "https://..."
    }
  ]
}
```

---

## 获取支持的 Kotlin 版本

```
GET /generator/kotlin-versions
```

### 响应

```json
{
  "success": true,
  "versions": ["2.0.0", "1.9.24", "1.9.23"]
}
```

---

## 获取支持的 Java 版本

```
GET /generator/java-versions
```

### 响应

```json
{
  "success": true,
  "versions": [21, 17, 11]
}
```

---

## 获取最新 API 版本

```
GET /generator/api-version
```

### 响应

```json
{
  "success": true,
  "version": "2.1.0"
}
```

---

## 验证项目配置

```
POST /generator/validate
```

### 请求体

| 字段 | 类型 | 必填 | 描述 |
|------|------|------|------|
| projectName | string | 是 | 项目名称，只能包含字母、数字、连字符 |
| groupId | string | 是 | 组织ID，如 com.example |
| version | string | 是 | 版本号，语义化版本 |
| description | string | 否 | 项目描述 |
| author | string | 否 | 作者名称 |
| projectType | ProjectType | 是 | 项目类型 |
| features | FeatureModule[] | 否 | 功能模块 |
| kotlinVersion | string | 是 | Kotlin 版本 |
| javaVersion | int | 是 | Java 版本 |
| includeExamples | boolean | 否 | 是否包含示例代码，默认 true |
| includeTests | boolean | 否 | 是否包含测试，默认 true |

### 响应

```json
{
  "success": true,
  "result": {
    "valid": true,
    "errors": [],
    "warnings": [
      "建议使用 Kotlin 2.0.0 以获得更好的性能"
    ]
  }
}
```

### 验证错误示例

```json
{
  "success": true,
  "result": {
    "valid": false,
    "errors": [
      {
        "field": "projectName",
        "message": "项目名称不能包含特殊字符"
      },
      {
        "field": "groupId",
        "message": "groupId 格式不正确"
      }
    ],
    "warnings": []
  }
}
```

---

## 预览生成的文件列表

```
POST /generator/preview
```

### 请求体

与 validate 接口相同。

### 响应

```json
{
  "success": true,
  "files": [
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
    "src/main/kotlin/com/example/myplugin/MyPlugin.kt",
    "src/main/kotlin/com/example/myplugin/command/MyCommand.kt",
    "src/main/resources/plugin.yml",
    "src/test/kotlin/com/example/myplugin/MyPluginTest.kt"
  ]
}
```

---

## 生成项目

```
POST /generator/generate
```

### 请求体

与 validate 接口相同。

### 响应

```json
{
  "success": true,
  "result": {
    "success": true,
    "files": [
      {
        "path": "build.gradle.kts",
        "content": "plugins {\n    ...\n}",
        "isDirectory": false
      },
      {
        "path": "src/main/kotlin",
        "content": "",
        "isDirectory": true
      }
    ],
    "downloadUrl": "https://cdn.azathoth.dev/generated/abc123.zip"
  }
}
```

### 错误响应

```json
{
  "success": false,
  "error": {
    "code": "GENERATION_FAILED",
    "message": "项目生成失败"
  }
}
```

---

## 下载生成的项目

```
GET /generator/download/{token}
```

### 响应

返回 zip 文件流。

```
Content-Type: application/zip
Content-Disposition: attachment; filename="my-plugin.zip"
```

---

## 生成示例

### 基础游戏插件

```json
{
  "projectName": "my-awesome-plugin",
  "groupId": "com.example",
  "version": "1.0.0",
  "description": "我的第一个Azathoth插件",
  "author": "developer",
  "projectType": "GAME_PLUGIN",
  "features": ["COMMAND_SYSTEM", "ITEM_SYSTEM"],
  "kotlinVersion": "2.0.0",
  "javaVersion": 21,
  "includeExamples": true,
  "includeTests": true
}
```

生成的目录结构：

```
my-awesome-plugin/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/myawesomeplugin/
│   │   │       ├── MyAwesomePlugin.kt
│   │   │       ├── command/
│   │   │       │   └── ExampleCommand.kt
│   │   │       └── item/
│   │   │           └── ExampleItem.kt
│   │   └── resources/
│   │       └── plugin.yml
│   └── test/
│       └── kotlin/
│           └── com/example/myawesomeplugin/
│               └── MyAwesomePluginTest.kt
└── README.md
```

### 完整 MMORPG 插件

```json
{
  "projectName": "epic-rpg-plugin",
  "groupId": "com.mygame",
  "version": "1.0.0-SNAPSHOT",
  "description": "完整的MMORPG游戏内容插件",
  "author": "GameDev",
  "projectType": "GAME_PLUGIN",
  "features": [
    "SKILL_SYSTEM",
    "DUNGEON_SYSTEM",
    "AI_BEHAVIOR",
    "ITEM_SYSTEM",
    "QUEST_SYSTEM",
    "COMMAND_SYSTEM",
    "DATABASE",
    "REDIS"
  ],
  "kotlinVersion": "2.0.0",
  "javaVersion": 21,
  "includeExamples": true,
  "includeTests": true
}
```
