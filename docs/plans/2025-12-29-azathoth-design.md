# Azathoth 框架设计文档

> MMORPG 级 Minecraft 服务器框架

## 概述

Azathoth 是一个基于 Minestom 的大规模 Minecraft MMORPG 服务器框架，采用微服务架构，支持几十万人同时在线，具备自动扩缩容能力。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 25 |
| 语言 | Kotlin | 2.3.0 |
| 构建 | Gradle (Kotlin DSL) | 9.2.1 |
| 游戏核心 | Minestom | 最新 |
| 通信 | gRPC-Kotlin | 1.5.0 |
| 后端框架 | Ktor | 3.3.3 |
| 前端框架 | Vue 3 | 3.5+ |
| 前端构建 | Rspack | 1.6.8 |
| 客户端 | Fabric | 跟随 MC 版本 |
| 编排 | Kubernetes + Agones | - |
| 消息队列 | Kafka | - |
| 数据库 | PostgreSQL, MongoDB, Redis, ClickHouse | - |

## 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                      Players (Fabric Client)                     │
└─────────────────────────┬───────────────────────────────────────┘
                          │ MC Protocol
┌─────────────────────────▼───────────────────────────────────────┐
│                      Gateway Service                             │
│            (连接管理、认证、负载均衡、无缝传送)                     │
└─────────────────────────┬───────────────────────────────────────┘
                          │ gRPC
┌─────────────────────────▼───────────────────────────────────────┐
│                    Game Instance Layer                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │ 主城实例  │  │ 副本实例  │  │ 副本实例  │  │   ...    │        │
│  │(Minestom)│  │(Minestom)│  │(Minestom)│  │          │        │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘        │
│                    Managed by Agones Fleet                       │
└──────────────────────┬──────────────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         │ gRPC (同步)    Kafka (异步) │
         ▼                           ▼
┌─────────────────┐    ┌──────────────────────────────────────────┐
│ Backend Services│    │              Kafka Cluster                │
│ ┌─────┐ ┌─────┐│    │  ┌────────────────────────────────────┐  │
│ │Player│ │Chat ││◄───┼──│ Topics: rewards, events, sync, logs│  │
│ └─────┘ └─────┘│    │  └────────────────────────────────────┘  │
│ ┌─────┐ ┌─────┐│    └──────────────────────────────────────────┘
│ │Dung.│ │Guild││
│ └─────┘ └─────┘│
└─────────────────┘
```

### 通信策略

- **gRPC**：玩家数据查询、权限验证、副本匹配、实时交互
- **Kafka**：奖励发放、跨服广播、活动通知、数据同步、日志收集

## 核心服务组件

### Gateway Service（网关服务）

| 职责 | 说明 |
|------|------|
| 连接管理 | MC 协议解析、连接维护 |
| 认证 | 自建账号系统、JWT Token |
| 路由 | 实例路由与负载均衡 |
| 传送 | 无缝传送协调 |

技术栈：Kotlin 协程 + Netty + gRPC Client + Redis

### Game Instance（游戏实例）

| 职责 | 说明 |
|------|------|
| 游戏逻辑 | 战斗、移动、交互 |
| 状态同步 | 玩家状态同步 |
| AI | BOSS/NPC AI |
| 副本 | 副本机制执行 |

技术栈：Minestom + Kotlin 协程 + Agones SDK + gRPC

### Player Service（玩家服务）

| 职责 | 存储 |
|------|------|
| 玩家档案 | PostgreSQL |
| 背包装备 | MongoDB |
| 在线缓存 | Redis |
| 交易记录 | PostgreSQL |

### Chat Service（聊天服务）

- 频道管理（世界、公会、队伍、私聊）
- 消息路由与持久化
- 敏感词过滤
- Kafka 订阅跨服消息

### Dungeon Service（副本服务）

- 副本模板管理
- 匹配队列（支持跨服匹配）
- 实例生命周期（Agones 分配/回收）
- 结算与奖励触发

### Activity Service（活动服务）

- 活动配置与调度
- 任务/成就追踪
- Kafka 发布/订阅事件

### Guild Service（公会服务）

- 公会创建/解散/管理
- 成员权限管理
- 公会活动/任务
- 公会仓库

## 数据流设计

### 同步流程（gRPC）

```
【玩家登录】
Client → Gateway → PlayerService.GetProfile()
                 → Response: 玩家数据
       ← Gateway ← 进入主城实例

【进入副本】
Client → GameInstance → DungeonService.RequestMatch()
                      → Response: 匹配成功/队列中
      ...匹配完成...
         DungeonService → Agones.Allocate()
                       → Gateway.TransferPlayer()
       ← Client 无缝传送到副本实例
```

### 异步流程（Kafka）

```
【副本结算】
DungeonInstance ─► Kafka [dungeon.completed] ─► PlayerService
                   {playerId, rewards, stats}   ├─► 更新玩家数据
                                                ├─► 发放奖励
                                                └─► ActivityService 更新成就

【跨服广播】
ActivityService ─► Kafka [activity.started] ─► 所有 GameInstance
                   {activityId, config}        ├─► 显示活动通知
                                               └─► 加载活动逻辑
```

### Kafka Topics

| Topic | 用途 |
|-------|------|
| player.rewards | 奖励发放 |
| player.sync | 玩家数据同步 |
| dungeon.completed | 副本结算 |
| activity.events | 活动事件 |
| chat.broadcast | 跨服聊天 |
| guild.events | 公会事件 |
| logs.* | 日志收集 |

## 数据存储设计

### PostgreSQL（结构化数据）

```sql
├── accounts          -- 账号信息
├── players           -- 玩家基础档案
├── transactions      -- 交易记录
├── guilds            -- 公会信息
├── guild_members     -- 公会成员关系
└── audit_logs        -- 审计日志
```

### MongoDB（文档数据）

```javascript
// 玩家背包
{
  playerId: "uuid",
  slots: [
    { slot: 0, itemId: "sword_001", count: 1, attributes: {...} }
  ]
}

// 副本配置
{
  dungeonId: "dragon_lair",
  difficulty: { easy: {...}, hard: {...} },
  drops: [...],
  boss: { phases: [...] }
}
```

### Redis（缓存与实时数据）

| Key 模式 | 用途 |
|----------|------|
| session:{token} | 会话信息 |
| online:{playerId} | 在线状态 + 所在实例 |
| player:{playerId} | 玩家数据缓存 |
| ranking:{type} | 排行榜 (Sorted Set) |
| lock:{resource} | 分布式锁 |
| matchmaking:{queue} | 匹配队列 |

### ClickHouse（分析数据）

| 表 | 用途 |
|----|------|
| player_actions | 玩家行为日志 |
| economy_flow | 经济流水 |
| dungeon_stats | 副本统计 |
| online_metrics | 在线人数时序 |

## 前端管理系统

### 技术架构

```
Admin Frontend (Vue 3 + Rspack + TypeScript)
              │ REST/WebSocket
              ▼
Admin Service (Ktor + Kotlin 协程)
├── AuthModule      -- 管理员认证、RBAC 权限
├── MonitorModule   -- 实时监控 WebSocket 推送
├── PlayerModule    -- 玩家管理 API
├── ActivityModule  -- 活动配置 API
├── ConfigModule    -- 游戏配置 API
└── AnalyticsModule -- 报表查询 API
```

### 功能模块

| 运维监控 | 游戏运营 |
|----------|----------|
| 服务器状态仪表盘 | 玩家查询/封禁/补偿 |
| 实例列表管理 | 邮件/公告发送 |
| 实时日志流 | 活动配置与调度 |
| 告警规则配置 | 商城/充值管理 |

| 数据分析 | 游戏配置 |
|----------|----------|
| 在线人数趋势图 | 副本编辑器 |
| 留存/付费漏斗 | 物品/装备编辑器 |
| 玩家行为热力图 | 技能/数值配置 |
| 自定义报表查询 | 掉落表配置 |

### 权限模型（RBAC）

| 角色 | 权限 |
|------|------|
| SuperAdmin | 全部权限 |
| Operator | 运营权限（玩家管理、活动、公告） |
| Designer | 策划权限（游戏配置） |
| Analyst | 分析权限（只读报表） |
| Monitor | 运维权限（监控、日志） |

## 容灾与高可用

### 服务高可用

| 组件 | 策略 |
|------|------|
| Gateway | 多副本（≥3）、K8s 负载均衡、自动重连 |
| Game Instance | Agones Fleet 缓冲池、自动补充、重连机制 |
| Backend Services | 无状态多副本、gRPC 重试、熔断器 |

### 数据容灾

| 存储 | 策略 |
|------|------|
| PostgreSQL | 主从复制（1主2从）、Patroni 自动故障转移、定时备份 |
| MongoDB | 副本集（3节点）、定时快照 |
| Redis | Sentinel/Cluster 模式、AOF 持久化 |
| Kafka | 3 Broker、副本因子 3、2 副本确认 |

### 异常处理

| 场景 | 策略 |
|------|------|
| 玩家操作失败 | 前置校验、重试+降级+补偿、幂等设计 |
| 副本实例崩溃 | 健康检查、标记不可用、部分奖励补偿 |

## 自动扩缩容

### Agones Fleet 配置

```yaml
apiVersion: agones.dev/v1
kind: Fleet
metadata:
  name: azathoth-game-instance
spec:
  replicas: 10
  scheduling: Packed
  template:
    spec:
      container: minestom
      resources:
        requests:
          cpu: "1"
          memory: "2Gi"
---
apiVersion: autoscaling.agones.dev/v1
kind: FleetAutoscaler
spec:
  fleetName: azathoth-game-instance
  policy:
    type: Buffer
    buffer:
      bufferSize: 5
      minReplicas: 10
      maxReplicas: 500
```

### 后端服务 HPA

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
spec:
  scaleTargetRef:
    name: player-service
  minReplicas: 3
  maxReplicas: 50
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          averageUtilization: 70
```

## 性能优化

### Kotlin 协程调度

```kotlin
val gameDispatcher = Dispatchers.Default.limitedParallelism(4)
val ioDispatcher = Dispatchers.IO.limitedParallelism(64)

suspend fun handlePlayerAction(player: Player, action: Action) {
    withContext(gameDispatcher) {
        // 游戏逻辑
    }
}

suspend fun savePlayerData(player: Player) {
    withContext(ioDispatcher) {
        // 异步持久化
    }
}
```

### 优化要点

- 对象池复用（减少 GC 压力）
- 批量数据库操作（减少 IO 次数）
- Redis Pipeline（批量缓存操作）
- gRPC 连接池复用
- Protobuf 序列化（高效编码）
- 分区处理（玩家按区域分片）

## 项目结构

```
azathoth/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
│
├── core/
│   ├── protocol/                 -- MC 协议定义
│   ├── common/                   -- 公共工具
│   ├── grpc-api/                 -- gRPC Proto 定义
│   └── kafka-events/             -- Kafka 事件定义
│
├── gateway/                      -- 网关服务
│
├── game-instance/                -- 游戏实例
│   ├── engine/                   -- Minestom 扩展
│   ├── mechanics/                -- 战斗、技能、AI
│   └── dungeons/                 -- 副本逻辑
│
├── services/
│   ├── player-service/
│   ├── chat-service/
│   ├── dungeon-service/
│   ├── activity-service/
│   ├── guild-service/
│   ├── trade-service/
│   └── admin-service/
│
├── admin-frontend/               -- Vue 管理前端
│
├── client-mod/                   -- Fabric 客户端模组
│
├── deploy/
│   ├── kubernetes/
│   ├── agones/
│   └── helm/
│
└── docs/
```

## 开发与测试

### 本地开发环境

```yaml
# docker-compose.dev.yml
services:
  - PostgreSQL
  - MongoDB
  - Redis
  - Kafka + Zookeeper
  - ClickHouse
```

### 测试策略

| 类型 | 工具/方法 |
|------|-----------|
| 单元测试 | JUnit 5 + MockK + runTest |
| 集成测试 | Testcontainers + gRPC E2E |
| 性能测试 | Gatling / k6 |
| 端到端测试 | 模拟客户端流程 |

### CI/CD

```
PR 提交 → 单元测试 → 构建镜像 → 集成测试
                                    ↓
生产部署 ← 人工审批 ← 预发布验证 ← 推送镜像
```

## 可扩展性设计

### 插件架构总览

```
┌─────────────────────────────────────────────────────────────────┐
│                      Azathoth Plugin System                      │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   Plugin Manager                         │    │
│  │  ├── PluginLoader (ClassLoader 隔离)                     │    │
│  │  ├── PluginRegistry (插件注册表)                         │    │
│  │  ├── DependencyResolver (依赖解析)                       │    │
│  │  └── HotSwapManager (热加载管理)                         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              │                                   │
│         ┌────────────────────┼────────────────────┐             │
│         ▼                    ▼                    ▼             │
│  ┌─────────────┐     ┌─────────────┐     ┌─────────────┐       │
│  │ Core Plugins│     │Game Plugins │     │ Ext Plugins │       │
│  │  (冷加载)    │     │  (热加载)    │     │  (热加载)    │       │
│  │  显式依赖    │     │ 服务发现     │     │ 服务发现     │       │
│  └─────────────┘     └─────────────┘     └─────────────┘       │
│        │                    │                    │              │
│        └────────────────────┼────────────────────┘              │
│                             ▼                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                  Service Registry                        │   │
│  │  (服务注册/发现/生命周期管理)                              │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

### 插件类型

| 类型 | 加载方式 | 依赖模式 | 示例 |
|------|----------|----------|------|
| Core | 冷加载 | 显式依赖 | 协议层、存储层、核心 API |
| Game | 热加载 | 服务发现 | 技能、副本、职业、AI |
| Extension | 热加载 | 服务发现 | 第三方支付、登录、监控 |

### 插件描述文件 (plugin.yml)

```yaml
# 基础信息
id: "skill-fireball"
name: "Fireball Skill"
version: "1.2.0"
description: "Adds fireball skill to the game"
author: "CommunityDev"

# 插件类型
type: game                    # core | game | extension

# 入口类
main: "com.example.FireballPlugin"

# API 版本兼容性
api-version: "1.0"

# 依赖声明 (Core 插件使用)
dependencies:
  required:
    - "azathoth-combat: ^2.0.0"
  optional:
    - "azathoth-particles: ^1.0.0"

# 服务声明 (Game/Extension 插件使用)
services:
  provides:
    - "com.azathoth.api.skill.SkillProvider"
  consumes:
    - "com.azathoth.api.combat.DamageCalculator"
    - "com.azathoth.api.effect.ParticleService?"  # ? 表示可选
```

### 插件生命周期

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ CREATED │───►│ LOADED  │───►│ ENABLED │───►│ DISABLED│
└─────────┘    └─────────┘    └─────────┘    └─────────┘
     │              │              │              │
     │              │              │              ▼
     │              │              │         ┌─────────┐
     └──────────────┴──────────────┴────────►│UNLOADED │
                                             └─────────┘

回调方法：
├── onLoad()      # 加载资源、注册服务
├── onEnable()    # 启动逻辑、订阅事件
├── onDisable()   # 停止逻辑、清理状态
└── onUnload()    # 释放资源、注销服务
```

### 插件基类

```kotlin
abstract class AzathothPlugin {
    lateinit var context: PluginContext

    // 生命周期回调
    open suspend fun onLoad() {}
    open suspend fun onEnable() {}
    open suspend fun onDisable() {}
    open suspend fun onUnload() {}

    // 服务注册
    protected fun <T : Any> registerService(
        serviceClass: KClass<T>,
        implementation: T
    )

    // 服务获取
    protected fun <T : Any> getService(
        serviceClass: KClass<T>
    ): T?

    protected fun <T : Any> requireService(
        serviceClass: KClass<T>
    ): T
}
```

### 服务注册与发现

```kotlin
interface ServiceRegistry {
    // 注册服务
    fun <T : Any> register(
        serviceClass: KClass<T>,
        implementation: T,
        provider: PluginInfo,
        priority: Int = 0
    )

    // 注销服务
    fun <T : Any> unregister(serviceClass: KClass<T>, provider: PluginInfo)

    // 获取服务（单个）
    fun <T : Any> get(serviceClass: KClass<T>): T?

    // 获取所有实现（多实现场景）
    fun <T : Any> getAll(serviceClass: KClass<T>): List<T>

    // 服务监听
    fun <T : Any> addListener(
        serviceClass: KClass<T>,
        listener: ServiceListener<T>
    )
}
```

### 游戏内容扩展 API

```
┌─────────────────────────────────────────────────────────────┐
│                    Game Content API                          │
├─────────────────────────────────────────────────────────────┤
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│  │  SkillAPI     │  │  DungeonAPI   │  │  EntityAPI    │   │
│  │  技能扩展      │  │  副本扩展      │  │  实体扩展      │   │
│  └───────────────┘  └───────────────┘  └───────────────┘   │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│  │  ItemAPI      │  │  QuestAPI     │  │  ClassAPI     │   │
│  │  物品扩展      │  │  任务扩展      │  │  职业扩展      │   │
│  └───────────────┘  └───────────────┘  └───────────────┘   │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│  │  AIAPI        │  │  EventAPI     │  │  CommandAPI   │   │
│  │  AI行为扩展    │  │  事件系统      │  │  命令扩展      │   │
│  └───────────────┘  └───────────────┘  └───────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

#### 技能扩展示例

```kotlin
@Skill(id = "fireball", name = "火球术")
class FireballSkill : AbstractSkill() {

    @SkillConfig
    var damage: Double = 50.0

    @SkillConfig
    var cooldown: Duration = 3.seconds

    override val manaCost: Int = 30

    override suspend fun onCast(context: SkillContext): SkillResult {
        val projectile = context.spawnProjectile<FireballProjectile> {
            speed = 2.0
            onHit { target ->
                context.dealDamage(target, damage, DamageType.FIRE)
                context.applyEffect(target, BurningEffect(3.seconds))
            }
        }
        return SkillResult.success()
    }
}
```

#### 副本扩展示例

```kotlin
@Dungeon(id = "dragon_lair", name = "龙穴")
class DragonLairDungeon : AbstractDungeon() {

    override val minPlayers = 5
    override val maxPlayers = 10
    override val timeLimit = 30.minutes

    override suspend fun onSetup(context: DungeonContext) {
        context.loadSchematic("dungeons/dragon_lair.schem")

        registerPhase(TrashMobPhase())
        registerPhase(MiniBossPhase())
        registerPhase(FinalBossPhase())
    }

    inner class FinalBossPhase : DungeonPhase("final_boss") {
        override suspend fun onStart(context: PhaseContext) {
            val boss = context.spawnBoss<DragonBoss>(bossSpawnLocation)
            boss.onDeath {
                context.complete(generateRewards())
            }
        }
    }
}
```

#### AI 行为扩展示例

```kotlin
@BehaviorTree(id = "dragon_ai")
class DragonAI : AbstractBehaviorTree() {

    override fun build(): BehaviorNode = selector {
        sequence {
            condition { entity.healthPercent < 0.2 }
            action { flee(duration = 5.seconds) }
        }
        sequence {
            condition { distanceToTarget > 10 }
            action { castSkill("fire_breath") }
        }
        action { meleeAttack() }
    }
}
```

### 服务层扩展 API

```
┌─────────────────────────────────────────────────────────────┐
│                   Service Extension API                      │
├─────────────────────────────────────────────────────────────┤
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│  │ PaymentSPI    │  │ AuthSPI       │  │ StorageSPI    │   │
│  │ 支付渠道扩展   │  │ 认证方式扩展   │  │ 存储后端扩展   │   │
│  └───────────────┘  └───────────────┘  └───────────────┘   │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│  │ MessageSPI    │  │ AnalyticsSPI  │  │ NotifySPI     │   │
│  │ 消息渠道扩展   │  │ 分析平台扩展   │  │ 通知渠道扩展   │   │
│  └───────────────┘  └───────────────┘  └───────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

#### 支付扩展示例

```kotlin
interface PaymentProvider {
    val providerId: String
    val displayName: String

    suspend fun createOrder(request: PaymentRequest): PaymentOrder
    suspend fun queryOrder(orderId: String): OrderStatus
    suspend fun handleCallback(payload: ByteArray): CallbackResult
}

@Extension(id = "payment-wechat")
class WechatPaymentPlugin : AzathothPlugin() {

    override suspend fun onLoad() {
        registerService(PaymentProvider::class, WechatPaymentProvider())
    }
}
```

#### 第三方登录扩展示例

```kotlin
interface AuthProvider {
    val providerId: String
    suspend fun authenticate(credentials: AuthCredentials): AuthResult
    suspend fun refreshToken(token: String): AuthResult
}

class QQAuthProvider : AuthProvider {
    override val providerId = "qq"

    override suspend fun authenticate(credentials: AuthCredentials): AuthResult {
        val qqToken = credentials as QQOAuthCredentials
        val userInfo = qqClient.getUserInfo(qqToken.accessToken)
        return AuthResult.success(
            userId = findOrCreateUser(userInfo),
            token = generateJwt(userInfo)
        )
    }
}
```

### 管理系统扩展 API

#### 后端扩展 (Ktor)

```kotlin
interface AdminModule {
    val moduleId: String
    val displayName: String
    val icon: String
    val permissions: List<String>

    fun Route.registerRoutes()
}

@Extension(id = "admin-lottery")
class LotteryAdminModule : AdminModule {
    override val moduleId = "lottery"
    override val displayName = "抽奖管理"
    override val icon = "gift"
    override val permissions = listOf("admin.lottery")

    override fun Route.registerRoutes() {
        route("/api/admin/lottery") {
            get("/list") { /* 获取抽奖活动列表 */ }
            post("/create") { /* 创建抽奖活动 */ }
            put("/{id}") { /* 更新抽奖活动 */ }
            delete("/{id}") { /* 删除抽奖活动 */ }
        }
    }
}
```

#### 前端扩展 (Vue)

```typescript
interface AdminPlugin {
  id: string
  name: string
  icon: string
  routes: RouteRecordRaw[]
  menuItems: MenuItem[]
  widgets?: DashboardWidget[]
}

export const lotteryPlugin: AdminPlugin = {
  id: 'lottery',
  name: '抽奖管理',
  icon: 'Gift',
  routes: [
    {
      path: '/lottery',
      component: () => import('./views/LotteryList.vue'),
      children: [
        { path: 'create', component: () => import('./views/LotteryCreate.vue') },
        { path: ':id/edit', component: () => import('./views/LotteryEdit.vue') },
      ]
    }
  ],
  menuItems: [
    { label: '抽奖活动', path: '/lottery', icon: 'Gift' }
  ],
  widgets: [
    { id: 'lottery-stats', component: LotteryStatsWidget, size: 'medium' }
  ]
}
```

#### 告警扩展

```kotlin
interface AlertNotifier {
    val notifierId: String
    suspend fun send(alert: Alert)
}

class DingTalkNotifier : AlertNotifier {
    override val notifierId = "dingtalk"

    override suspend fun send(alert: Alert) {
        dingTalkClient.sendMarkdown(
            title = "[${alert.level}] ${alert.title}",
            content = alert.toMarkdown()
        )
    }
}
```

### 事件系统

```
┌─────────────────────────────────────────────────────────────┐
│                      Event Bus                               │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │ Game Events │    │Service Events│   │System Events│     │
│  │ 游戏事件     │    │ 服务事件     │    │ 系统事件    │     │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘     │
│         └──────────────────┼──────────────────┘             │
│                            ▼                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Event Dispatcher                        │   │
│  │  ├── 优先级排序                                       │   │
│  │  ├── 异步/同步分发                                    │   │
│  │  ├── 事件取消机制                                     │   │
│  │  └── 错误隔离                                        │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

#### 事件定义

```kotlin
abstract class AzathothEvent {
    val timestamp: Instant = Instant.now()
    var cancelled: Boolean = false
}

interface Cancellable {
    var cancelled: Boolean
}

class PlayerDamageEvent(
    val player: Player,
    val source: DamageSource,
    var damage: Double
) : AzathothEvent(), Cancellable

class PlayerLevelUpEvent(
    val player: Player,
    val oldLevel: Int,
    val newLevel: Int
) : AzathothEvent()
```

#### 事件监听

```kotlin
class CombatPlugin : AzathothPlugin() {

    override suspend fun onEnable() {
        context.events.register(this)
    }

    @EventHandler(priority = EventPriority.HIGH)
    suspend fun onPlayerDamage(event: PlayerDamageEvent) {
        val armor = event.player.getArmor()
        event.damage *= (1 - armor.reduction)

        if (event.player.hasEffect(InvincibleEffect::class)) {
            event.cancelled = true
        }
    }

    @EventHandler(async = true)
    suspend fun onLevelUp(event: PlayerLevelUpEvent) {
        broadcastService.announce("${event.player.name} 升到了 ${event.newLevel} 级！")
    }
}
```

### 钩子机制

```kotlin
interface Hook<T> {
    suspend fun execute(context: T): T
}

class DamageCalculationHooks {
    private val hooks = mutableListOf<Hook<DamageContext>>()

    fun register(hook: Hook<DamageContext>, priority: Int = 0)

    suspend fun process(context: DamageContext): DamageContext {
        return hooks.sortedBy { it.priority }
            .fold(context) { ctx, hook -> hook.execute(ctx) }
    }
}

class ElementalDamageHook : Hook<DamageContext> {
    override suspend fun execute(context: DamageContext): DamageContext {
        val multiplier = getElementalMultiplier(
            context.source.element,
            context.target.element
        )
        return context.copy(damage = context.damage * multiplier)
    }
}
```

### 开发者 SDK

```
azathoth-sdk/
├── azathoth-api/                 # 核心 API（只读接口）
│   ├── player-api/
│   ├── combat-api/
│   ├── dungeon-api/
│   └── service-api/
│
├── azathoth-plugin-api/          # 插件开发 API
│   ├── plugin-core/
│   ├── plugin-events/
│   ├── plugin-services/
│   └── plugin-config/
│
├── azathoth-testing/             # 测试工具
│   ├── mock-server/
│   ├── mock-player/
│   └── test-utils/
│
└── azathoth-gradle-plugin/       # Gradle 构建插件
    ├── plugin-packaging/
    └── plugin-publishing/
```

#### Gradle 插件使用

```kotlin
plugins {
    id("com.azathoth.plugin") version "1.0.0"
}

azathothPlugin {
    id = "my-awesome-plugin"
    version = "1.0.0"
    apiVersion = "1.0"
    generatePluginYml = true
    shadowDependencies = true
}

dependencies {
    compileOnly("com.azathoth:azathoth-api:1.0.0")
    compileOnly("com.azathoth:azathoth-plugin-api:1.0.0")
    testImplementation("com.azathoth:azathoth-testing:1.0.0")
}
```

#### 插件测试框架

```kotlin
class FireballSkillTest {

    @Test
    fun `fireball should deal fire damage`() = runAzathothTest {
        val server = MockServer()
        val player = server.createPlayer("TestPlayer")
        val target = server.createEntity<Zombie>()

        server.loadPlugin<FireballPlugin>()

        val skill = server.getService<SkillProvider>()
            .find { it.skillId == "fireball" }!!
        val result = skill.cast(player, target)

        assertThat(result).isSuccess()
        assertThat(target.lastDamage).isCloseTo(50.0, 0.1)
        assertThat(target.lastDamageType).isEqualTo(DamageType.FIRE)
    }
}
```

### 开发者文档（中英双语）

```
docs.azathoth.dev/
├── 🌐 语言切换 (zh-CN / en-US)
│
├── zh-CN/                          # 中文文档
│   ├── 快速开始/
│   │   ├── 快速入门指南
│   │   ├── 项目配置
│   │   └── 第一个插件教程
│   ├── 核心概念/
│   ├── API 参考/
│   ├── 开发教程/
│   └── 示例代码/
│
├── en-US/                          # English Docs
│   ├── Getting Started/
│   │   ├── Quick Start Guide
│   │   ├── Project Setup
│   │   └── First Plugin Tutorial
│   ├── Core Concepts/
│   ├── API Reference/
│   ├── Tutorials/
│   └── Examples/
│
└── shared/                         # 共享资源
    ├── images/
    ├── diagrams/
    └── code-samples/
```

#### API 注释规范

```kotlin
/**
 * 技能提供者接口
 * Skill provider interface
 *
 * 实现此接口以创建自定义技能
 * Implement this interface to create custom skills
 *
 * @see AbstractSkill
 * @since 1.0.0
 */
interface SkillProvider {
    /**
     * 技能唯一标识 / Unique skill identifier
     */
    val skillId: String

    /**
     * 施放技能 / Cast the skill
     *
     * @param caster 施法者 / The caster
     * @param target 目标 / The target
     * @return 技能结果 / Skill result
     */
    suspend fun cast(caster: Player, target: Entity): SkillResult
}
```

### 可扩展性设计总结

| 方面 | 决策 |
|------|------|
| 插件类型 | Core（冷加载）+ Game/Extension（热加载） |
| 隔离策略 | ClassLoader 隔离 |
| 依赖模式 | 混合（核心显式依赖 + 扩展服务发现） |
| API 开放 | 公开 API + 双语开发者文档 |
| 游戏扩展 | Skill / Dungeon / Entity / Item / Quest / Class / AI |
| 服务扩展 | Payment / Auth / Storage / Message / Analytics / Notify |
| 管理扩展 | AdminModule (Ktor) + AdminPlugin (Vue) + Metrics/Alert |
| 事件系统 | 优先级 + 异步/同步 + 可取消 + 钩子链 |
| SDK | API + Plugin API + Testing + Gradle Plugin |
| 文档语言 | 中文 + 英文双语 |

## 官网平台设计

### 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                    azathoth.dev (Nuxt 3)                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ Landing  │ │   Docs   │ │  Market  │ │  Forum   │           │
│  │  首页     │ │  文档     │ │  市场     │ │  社区     │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │Generator │ │ Tutorial │ │  API Ref │ │  User    │           │
│  │ 项目生成  │ │  教程     │ │ API文档   │ │ 用户中心  │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Website Backend (Ktor)                        │
├─────────────────────────────────────────────────────────────────┤
│  ├── AuthModule        # 用户认证（与游戏账号统一）               │
│  ├── ContentModule     # CMS 内容管理                           │
│  ├── MarketModule      # 资源市场                               │
│  ├── PaymentModule     # 支付/提现                              │
│  ├── ForumModule       # 社区论坛                               │
│  ├── GeneratorModule   # 项目生成器                             │
│  └── ReviewModule      # 资源审核                               │
└─────────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        ┌──────────┐   ┌──────────┐   ┌──────────┐
        │PostgreSQL│   │  Redis   │   │   OSS    │
        │ 业务数据  │   │ 缓存/会话 │   │ 资源存储  │
        └──────────┘   └──────────┘   └──────────┘
```

### 开发者市场

#### 资源类型

| 类型 | 说明 |
|------|------|
| Plugin | 游戏插件（技能、副本、玩法） |
| Module | 功能模块（支付、登录、分析） |
| Service | 独立服务（微服务组件） |
| Template | 项目模板/脚手架 |
| Theme | 管理后台主题 |
| Tool | 开发工具/脚本 |

#### 授权类型

| 类型 | 说明 | 价格 |
|------|------|------|
| 免费开源 | MIT/Apache 协议，含源码 | ¥0 |
| 免费闭源 | 仅限使用，不含源码 | ¥0 |
| 付费买断 | 一次购买永久使用 | 作者定价 |
| 付费订阅 | 按月/年付费，含持续更新 | 作者定价 |

#### 资源数据模型

```kotlin
data class MarketResource(
    val id: String,
    val name: String,
    val slug: String,
    val description: String,
    val type: ResourceType,
    val license: LicenseType,

    // 作者信息
    val authorId: String,

    // 版本管理
    val versions: List<ResourceVersion>,
    val latestVersion: String,

    // 定价
    val pricing: Pricing?,

    // 统计
    val downloads: Long,
    val rating: Double,
    val reviewCount: Int,

    // 审核状态
    val status: ResourceStatus,      // PENDING / APPROVED / REJECTED

    // 兼容性
    val minApiVersion: String,
    val maxApiVersion: String?,
    val dependencies: List<String>,

    // 展示
    val icon: String,
    val screenshots: List<String>,
    val tags: List<String>,

    val createdAt: Instant,
    val updatedAt: Instant
)
```

#### 交易流程

```
【购买流程】
用户 → 选择资源 → 确认订单 → 选择支付方式
                              ↓
              ┌───────────────┴───────────────┐
              ▼                               ▼
         微信支付                          支付宝
              │                               │
              └───────────────┬───────────────┘
                              ▼
                    支付成功 → 生成授权 → 可下载

【提现流程】
作者 → 申请提现 → 审核（≤24h）→ 打款到账
                      ↓
               平台扣除抽成
```

### 社区论坛

#### 功能设计

```
├── 板块分类
│   ├── 公告通知        # 官方公告、更新日志
│   ├── 技术交流        # 开发讨论、问答
│   ├── 资源分享        # 免费资源、代码片段
│   ├── 作品展示        # 项目展示、案例分享
│   └── 反馈建议        # Bug 反馈、功能建议
│
├── 帖子功能
│   ├── 富文本编辑（Markdown + 代码高亮）
│   ├── 图片/附件上传
│   ├── @提及用户
│   ├── 标签分类
│   └── 置顶/精华
│
├── 互动功能
│   ├── 回复/楼中楼
│   ├── 点赞/收藏
│   ├── 关注用户/板块
│   └── 消息通知
│
└── 用户等级
    ├── 经验值系统（发帖、回复、被点赞）
    ├── 等级徽章
    └── 开发者认证标识
```

#### 数据模型

```kotlin
data class ForumPost(
    val id: String,
    val title: String,
    val content: String,
    val authorId: String,
    val categoryId: String,
    val tags: List<String>,

    val isPinned: Boolean,
    val isFeatured: Boolean,
    val isLocked: Boolean,

    val viewCount: Long,
    val likeCount: Int,
    val replyCount: Int,

    val createdAt: Instant,
    val updatedAt: Instant,
    val lastReplyAt: Instant?
)
```

### 项目生成器

#### 界面设计

```
┌─────────────────────────────────────────────────────────────┐
│                   Azathoth Initializer                       │
├─────────────────────────────────────────────────────────────┤
│  项目信息                                                    │
│  ├── 项目名称: [my-awesome-plugin    ]                      │
│  ├── Group ID: [com.example          ]                      │
│  ├── 版本:     [1.0.0                ]                      │
│  └── 描述:     [My first plugin      ]                      │
├─────────────────────────────────────────────────────────────┤
│  插件类型                                                    │
│  ○ Game Plugin（游戏内容插件）                               │
│  ○ Extension Plugin（扩展服务插件）                          │
│  ○ Admin Module（管理后台模块）                              │
├─────────────────────────────────────────────────────────────┤
│  功能模块（可多选）                                          │
│  ☐ 技能系统  ☐ 副本系统  ☐ AI 行为                          │
│  ☐ 物品系统  ☐ 任务系统  ☐ 命令系统                          │
│  ☐ 数据库集成 ☐ Redis集成 ☐ gRPC客户端                       │
├─────────────────────────────────────────────────────────────┤
│  构建配置                                                    │
│  ├── Kotlin 版本: [2.3.0 ▼]                                 │
│  ├── Java 版本:   [25 ▼]                                    │
│  └── ☑ 包含示例代码                                         │
├─────────────────────────────────────────────────────────────┤
│                    [ 生成项目 ↓ ]                            │
└─────────────────────────────────────────────────────────────┘
```

#### 生成的多模块项目结构

```
my-awesome-plugin/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
│
├── core/                         # :core 模块
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── com/example/core/
│           ├── util/
│           └── config/
│
├── api/                          # :api 模块
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       └── com/example/api/
│           ├── service/
│           └── model/
│
├── skills/                       # :skills 模块（如选中）
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/
│       │   └── com/example/skills/
│       └── test/kotlin/
│
├── plugin/                       # :plugin 打包模块
│   ├── build.gradle.kts
│   └── src/main/
│       ├── kotlin/
│       │   └── com/example/
│       │       └── MyAwesomePlugin.kt
│       └── resources/
│           └── plugin.yml
│
├── .idea/
├── README.md
└── .gitignore
```

#### 模块依赖关系

```
                    ┌─────────┐
                    │  :api   │
                    └────┬────┘
                         │
         ┌───────────────┼───────────────┐
         ▼               ▼               ▼
    ┌─────────┐    ┌─────────┐    ┌─────────┐
    │ :skills │    │:database│    │  :core  │
    └────┬────┘    └────┬────┘    └────┬────┘
         │               │               │
         └───────────────┼───────────────┘
                         ▼
                   ┌──────────┐
                   │ :plugin  │  ← 聚合打包
                   └──────────┘
```

### Landing Page

```
┌─────────────────────────────────────────────────────────────┐
│  Hero Section                                                │
│  ├── 标语 + 简介                                             │
│  ├── 快速开始按钮                                            │
│  └── GitHub Star / 下载量统计                                │
├─────────────────────────────────────────────────────────────┤
│  Features（特性展示）                                         │
│  ├── 高性能微服务架构                                         │
│  ├── 自动扩缩容                                              │
│  ├── 插件生态                                                │
│  └── 可视化管理                                              │
├─────────────────────────────────────────────────────────────┤
│  Showcase（案例展示）                                         │
├─────────────────────────────────────────────────────────────┤
│  Sponsors（赞助商）                                          │
├─────────────────────────────────────────────────────────────┤
│  Footer（页脚）                                              │
└─────────────────────────────────────────────────────────────┘
```

### 用户中心

```
├── 个人资料
│   ├── 头像/昵称/简介
│   ├── 社交链接（GitHub、网站）
│   └── 开发者认证
│
├── 我的资源
│   ├── 已发布资源列表
│   ├── 资源数据统计
│   └── 新建/编辑资源
│
├── 财务中心
│   ├── 余额查询
│   ├── 充值记录
│   ├── 收益明细
│   ├── 提现申请
│   └── 购买记录
│
├── 消息通知
│   ├── 系统通知
│   ├── 交易通知
│   ├── 评论/回复
│   └── 关注动态
│
└── 账号设置
    ├── 修改密码
    ├── 绑定邮箱/手机
    └── 账号安全
```

### 官网项目结构

```
azathoth-website/
├── apps/
│   ├── web/                        # Nuxt 3 主站
│   │   ├── pages/
│   │   │   ├── index.vue           # Landing Page
│   │   │   ├── docs/[...slug].vue  # 文档页面
│   │   │   ├── market/             # 资源市场
│   │   │   ├── forum/              # 社区论坛
│   │   │   ├── generator/          # 项目生成器
│   │   │   └── user/               # 用户中心
│   │   ├── components/
│   │   ├── composables/
│   │   ├── layouts/
│   │   └── nuxt.config.ts
│   │
│   └── backend/                    # Ktor 后端
│       ├── src/main/kotlin/
│       │   └── com/azathoth/website/
│       │       ├── Application.kt
│       │       ├── modules/
│       │       │   ├── auth/
│       │       │   ├── market/
│       │       │   ├── forum/
│       │       │   ├── payment/
│       │       │   ├── generator/
│       │       │   └── review/
│       │       └── plugins/
│       └── build.gradle.kts
│
├── packages/
│   ├── ui/                         # 共享 UI 组件
│   ├── api-client/                 # API 客户端
│   └── shared/                     # 共享类型/工具
│
├── content/                        # 文档内容 (Markdown)
│   ├── zh-CN/
│   └── en-US/
│
├── docker-compose.yml
├── package.json
└── pnpm-workspace.yaml
```

### 官网技术栈

| 层级 | 技术 |
|------|------|
| 前端框架 | Nuxt 3 |
| UI 组件 | Nuxt UI / Tailwind CSS |
| 文档渲染 | @nuxt/content |
| 代码编辑 | Monaco Editor |
| 后端框架 | Ktor 3.3.3 |
| 数据库 | PostgreSQL |
| 缓存 | Redis |
| 文件存储 | MinIO / 阿里云 OSS |
| 支付 | 微信支付 / 支付宝 |

### 官网平台设计总结

| 模块 | 功能 |
|------|------|
| Landing Page | 框架宣传、特性展示、案例展示 |
| 文档系统 | 中英双语、API 文档、教程 |
| 项目生成器 | 多模块脚手架、模块勾选、一键下载 |
| 开发者市场 | 资源发布、付费/免费、先审后发 |
| 交易系统 | 微信/支付宝充值、平台抽成、提现 |
| 社区论坛 | 技术交流、问答、作品展示 |
| 用户中心 | 个人资料、我的资源、财务中心 |

| 决策项 | 选择 |
|--------|------|
| 主站框架 | Nuxt 3 |
| 后端框架 | Ktor |
| 部署方式 | 自有服务器 |
| 支付渠道 | 微信 + 支付宝 |
| 提现渠道 | 支付宝 + 银行卡 |
| 交易模式 | 平台抽成 |
| 审核机制 | 先审后发 |
| 授权类型 | 固定四种（免费开源/闭源、付费买断/订阅） |
| 项目生成 | 多模块 Gradle 项目 |

---

*文档生成日期：2025-12-29*
