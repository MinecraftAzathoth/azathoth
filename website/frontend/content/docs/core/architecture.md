---
title: '架构设计'
description: '深入了解 Azathoth 的模块化架构设计'
navigation:
  order: 1
---

# 架构设计

Azathoth 采用现代化的模块化架构，旨在提供高度可扩展、易于测试的开发体验。

## 核心设计原则

### 模块化

每个功能都是独立的模块，可以按需加载和卸载。这种设计带来以下优势：

- **按需加载**：只加载需要的功能，减少资源占用
- **热更新**：支持运行时更新模块
- **隔离性**：模块间通过接口通信，降低耦合

### 依赖注入

基于 Koin 的依赖注入系统，实现了：

- **松耦合**：组件之间不直接依赖具体实现
- **可测试**：易于替换为测试替身
- **生命周期管理**：自动管理对象的创建和销毁

### 事件驱动

事件总线贯穿整个框架：

- **异步处理**：支持异步事件处理
- **优先级控制**：可设置事件处理器优先级
- **取消机制**：支持取消事件传播

## 分层架构

```
┌─────────────────────────────────────────┐
│              Plugin Layer               │
│         (用户开发的插件)                  │
├─────────────────────────────────────────┤
│              API Layer                  │
│         (公开的 API 接口)                │
├─────────────────────────────────────────┤
│            Service Layer                │
│         (核心服务实现)                   │
├─────────────────────────────────────────┤
│             Core Layer                  │
│         (框架核心运行时)                 │
├─────────────────────────────────────────┤
│           Platform Layer                │
│         (Bukkit/Paper 适配)              │
└─────────────────────────────────────────┘
```

### Plugin Layer

用户开发的插件所在层，通过 API Layer 与框架交互。

### API Layer

定义所有公开的接口和数据类型，保证 API 稳定性。

### Service Layer

实现核心业务逻辑，如技能系统、副本系统等。

### Core Layer

框架的核心运行时，包括：

- 插件加载器
- 依赖注入容器
- 事件总线
- 配置管理

### Platform Layer

与 Minecraft 服务端的适配层，屏蔽平台差异。

## 核心组件

### PluginManager

管理插件的加载、启用、禁用和卸载：

```kotlin
interface PluginManager {
    fun loadPlugin(file: File): AzathothPluginDescriptor
    fun enablePlugin(id: String): Boolean
    fun disablePlugin(id: String): Boolean
    fun unloadPlugin(id: String): Boolean
    fun getPlugin(id: String): AzathothPlugin?
    fun getAllPlugins(): List<AzathothPlugin>
}
```

### ServiceRegistry

服务注册中心，管理所有服务的注册和查找：

```kotlin
interface ServiceRegistry {
    fun <T : Any> register(type: KClass<T>, implementation: T)
    fun <T : Any> get(type: KClass<T>): T?
    fun <T : Any> getOrThrow(type: KClass<T>): T
}
```

### EventBus

事件发布订阅系统：

```kotlin
interface EventBus {
    fun <T : AzathothEvent> subscribe(
        eventType: KClass<T>,
        priority: EventPriority = EventPriority.NORMAL,
        handler: (T) -> Unit
    ): EventSubscription

    fun <T : AzathothEvent> publish(event: T)
}
```

## 模块系统

每个模块都实现 `AzathothModule` 接口：

```kotlin
interface AzathothModule {
    val id: String
    val name: String
    val dependencies: List<String>

    fun onLoad(context: ModuleContext)
    fun onEnable()
    fun onDisable()
    fun onUnload()
}
```

### 模块依赖

模块可以声明对其他模块的依赖：

```kotlin
@Module(
    id = "dungeon",
    name = "Dungeon System",
    dependencies = ["skill", "item"]
)
class DungeonModule : AzathothModule {
    // ...
}
```

框架会自动处理依赖顺序，确保依赖模块先于被依赖模块加载。

## 扩展点

Azathoth 提供多种扩展机制：

### 服务扩展

通过实现服务接口提供自定义实现：

```kotlin
@Service(priority = ServicePriority.HIGH)
class CustomSkillService : SkillService {
    // 自定义技能服务实现
}
```

### 事件扩展

通过事件监听器扩展功能：

```kotlin
@EventListener
class MyEventListener {
    @Subscribe
    fun onSkillCast(event: SkillCastEvent) {
        // 处理技能释放事件
    }
}
```

### 命令扩展

注册自定义命令：

```kotlin
@Command("mycommand")
class MyCommand : AzathothCommand() {
    override fun execute(sender: CommandSender, args: Array<String>) {
        // 命令逻辑
    }
}
```

## 下一步

- [生命周期](/docs/core/lifecycle) - 了解插件和模块的生命周期
- [依赖注入](/docs/core/dependency-injection) - 深入理解 DI 系统
- [事件系统](/docs/core/events) - 掌握事件驱动开发
