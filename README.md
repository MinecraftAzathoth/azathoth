<div align="center">

# Azathoth

[![GitHub stars](https://img.shields.io/github/stars/MinecraftAzathoth/azathoth?style=flat-square&logo=github)](https://github.com/MinecraftAzathoth/azathoth/stargazers)
[![Website](https://img.shields.io/badge/官网-mcwar.cn-FF6B6B?style=flat-square&logo=globe&logoColor=white)](https://www.mcwar.cn)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Minestom](https://img.shields.io/badge/Minestom-latest-00AA00?style=flat-square)](https://minestom.net/)
[![License](https://img.shields.io/badge/License-ANCL-red.svg?style=flat-square)](LICENSE)

**MMORPG 级 Minecraft 服务器框架**

*为下一代大型多人在线游戏而生，支持几十万玩家同时在线*

[English](README_EN.md) | 简体中文

[官方网站](https://www.mcwar.cn) · [开发文档](https://docs.mcwar.cn) · [问题反馈](https://github.com/MinecraftAzathoth/azathoth/issues)

</div>

---

## 项目愿景

想象一下，在 Minecraft 的世界中构建一个真正的 MMORPG——拥有史诗级副本、公会战争、实时经济系统，同时容纳数十万玩家在同一个宇宙中冒险。

**Azathoth 让这一切成为可能。**

我们不仅仅是另一个服务器框架。Azathoth 是一套完整的游戏基础设施解决方案，从底层协议到云端部署，从插件开发到运营管理，为开发者提供构建大规模在线游戏所需的一切。

```
┌─────────────────────────────────────────────────────────────────┐
│                      Players (Fabric Client)                     │
└─────────────────────────┬───────────────────────────────────────┘
                          │ MC Protocol
┌─────────────────────────▼───────────────────────────────────────┐
│                      Gateway Service                             │
│              连接管理 · 智能路由 · 无缝传送 · 负载均衡              │
└─────────────────────────┬───────────────────────────────────────┘
                          │ gRPC
┌─────────────────────────▼───────────────────────────────────────┐
│                    Game Instance Layer                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │  主城实例 │  │ 副本实例  │  │ 战场实例  │  │   ...    │         │
│  │(Minestom)│  │(Minestom)│  │(Minestom)│  │          │         │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
│                    Managed by Agones Fleet                       │
└──────────────────────┬──────────────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         │ gRPC (同步)    Kafka (异步) │
         ▼                           ▼
┌─────────────────┐    ┌──────────────────────────────────────────┐
│ Backend Services│    │              Message Bus                  │
│ ├─ Player       │    │      跨服广播 · 事件驱动 · 数据同步         │
│ ├─ Dungeon      │    └──────────────────────────────────────────┘
│ ├─ Guild        │
│ └─ ...          │
└─────────────────┘
```

---

## 功能亮点

### 为大规模而生

<table>
<tr>
<td width="50%">

#### 几十万人同时在线
基于微服务架构设计，每个组件独立扩展。Gateway 处理连接，Game Instance 运行游戏逻辑，Backend Services 管理业务数据——三层解耦，线性扩展。

</td>
<td width="50%">

#### 基于 Minestom 的高性能核心
抛弃 Bukkit 的历史包袱，使用 Minestom 从零构建。无 Tick 限制，纯异步设计，单实例可承载数千玩家。协程优先的 API 让高并发编程如同写同步代码。

</td>
</tr>
<tr>
<td width="50%">

#### Agones 自动扩缩容
游戏实例由 Kubernetes + Agones 编排。玩家涌入时自动扩容，空闲时自动缩减。Buffer 池确保新实例即时可用，告别手动运维。

</td>
<td width="50%">

#### 热加载插件系统
三类插件满足不同需求：Core（冷加载核心）、Game（热加载游戏内容）、Extension（热加载扩展服务）。修改技能参数？更新副本配置？无需重启，即时生效。

</td>
</tr>
<tr>
<td width="50%">

#### gRPC + Kafka 混合通信
同步调用用 gRPC——低延迟、强类型、双向流。异步消息用 Kafka——高吞吐、持久化、完美解耦。根据场景选择最优方案。

</td>
<td width="50%">

#### 完整的管理后台
Vue 3 + Ktor 构建的现代化管理系统。实时监控服务器状态，一键管理玩家数据，可视化配置游戏内容。RBAC 权限模型，安全可控。

</td>
</tr>
<tr>
<td colspan="2">

#### 开发者市场生态
不止是框架，更是生态。官方市场支持插件发布、交易、评价。免费开源或付费授权，开发者自由选择。完善的 SDK 和中英双语文档降低上手门槛。

</td>
</tr>
</table>

---

## 技术栈

| 类别 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **语言** | Kotlin | 2.3.0 | 主要开发语言，协程优先 |
| **语言** | Java | 25 | 底层兼容 |
| **构建** | Gradle (Kotlin DSL) | 9.2.1 | 多模块构建 |
| **游戏核心** | Minestom | latest | 高性能 MC 服务器库 |
| **通信** | gRPC-Kotlin | 1.5.0 | 服务间同步调用 |
| **消息队列** | Kafka | 4.1+ | 异步消息传递 |
| **后端框架** | Ktor | 3.3.3 | 管理后台 API |
| **前端框架** | Vue 3 | 3.5+ | 管理后台 UI |
| **编排** | Kubernetes + Agones | - | 容器编排与游戏服务器管理 |
| **数据库** | PostgreSQL | 17+ | 结构化数据存储 |
| **数据库** | MongoDB | 8+ | 文档数据存储 |
| **数据库** | ClickHouse | 24.8+ | 玩家快照与回档时序数据 |
| **缓存** | Redis | 7+ | 会话、缓存、排行榜 |
| **构建工具** | Rspack / Rsbuild | latest | 前端构建 |

---

## 快速开始

### 环境要求

- **JDK 21+** (推荐 [Eclipse Temurin](https://adoptium.net/)，生产环境推荐 JDK 25)
- **Docker & Docker Compose**
- **Node.js 20+** (管理后台前端)
- **Gradle 9.2.1+** (项目自带 Wrapper，无需手动安装)

### 本地开发环境

```bash
# 1. 克隆项目
git clone https://github.com/MinecraftAzathoth/azathoth.git
cd azathoth

# 2. 启动基础设施 (PostgreSQL, MongoDB, Redis, Kafka, ClickHouse)
docker-compose -f deploy/docker/docker-compose.yml up -d

# 3. 构建项目
./gradlew build

# 4. 运行网关服务
./gradlew :gateway:run

# 5. 运行游戏实例 (新终端)
./gradlew :game-instance:run

# 6. 运行管理后台前端 (新终端)
cd admin-frontend
npm install
npm run dev
```

### 常用构建命令

```bash
./gradlew build                    # 构建整个项目
./gradlew :gateway:build           # 构建单个模块
./gradlew test                     # 运行全部测试
./gradlew :game-instance:test      # 运行单个模块测试
./gradlew :gateway:shadowJar       # 生成可执行 JAR
```

### 插件开发

```kotlin
// build.gradle.kts
plugins {
    id("com.azathoth.plugin") version "1.0.0"
}

dependencies {
    compileOnly("com.azathoth:azathoth-api:1.0.0")
    compileOnly("com.azathoth:azathoth-plugin-api:1.0.0")
}
```

### 集群部署

```bash
# 1. 配置 Kubernetes 集群 (需要预先安装 Agones)
kubectl apply -f deploy/agones/

# 2. 部署游戏服务
helm install azathoth deploy/helm/azathoth/

# 3. 查看状态
kubectl get fleet -n azathoth
```

详细部署指南请参阅 [部署文档](https://docs.mcwar.cn/deployment)。

---

## 使用示例

### 创建技能插件

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

        // 播放音效和粒子
        context.playSound(Sound.ENTITY_BLAZE_SHOOT)
        context.spawnParticles(Particle.FLAME, count = 20)

        return SkillResult.success()
    }
}
```

### 创建副本

```kotlin
@Dungeon(id = "dragon_lair", name = "龙穴")
class DragonLairDungeon : AbstractDungeon() {

    override val minPlayers = 5
    override val maxPlayers = 10
    override val timeLimit = 30.minutes

    override suspend fun onSetup(context: DungeonContext) {
        // 加载地图
        context.loadSchematic("dungeons/dragon_lair.schem")

        // 注册阶段
        registerPhase(TrashMobPhase())
        registerPhase(MiniBossPhase())
        registerPhase(FinalBossPhase())
    }

    inner class FinalBossPhase : DungeonPhase("final_boss") {
        override suspend fun onStart(context: PhaseContext) {
            context.announce("巨龙苏醒了！")

            val boss = context.spawnBoss<DragonBoss>(bossSpawnLocation) {
                level = context.difficulty.bossLevel
                lootTable = "dragon_lair_final"
            }

            boss.onDeath {
                context.complete(generateRewards())
            }
        }
    }
}
```

### 创建 AI 行为树

```kotlin
// 使用 DSL 构建行为树
val dragonAI = behaviorTree("dragon_ai") {
    selector("root") {
        // 低血量时逃跑并治疗
        sequence("fleeAndHeal") {
            node(IsHealthBelow(0.2))
            node(Flee(speed = 0.25, safeDistance = 20.0))
            node(Heal(amount = 50.0, cooldownTicks = 100))
        }

        // 远程攻击（射程内直接攻击）
        sequence("rangedCombat") {
            node(HasTarget())
            node(IsTargetAlive())
            node(IsTargetInRange(16.0))
            node(RangedAttack(damage = 15.0, range = 16.0))
        }

        // 近战攻击
        sequence("meleeCombat") {
            node(HasTarget())
            node(IsTargetAlive())
            node(IsTargetInRange(3.0))
            node(MeleeAttack(damage = 25.0, range = 3.0))
        }

        // 追击目标
        sequence("chase") {
            node(FindTarget(perception, range = 24.0))
            node(ChaseTarget(speed = 0.3, arrivalDistance = 3.0))
        }
    }
}

// 或使用预制模板快速创建
val zombieAI = BehaviorTemplates.aggressiveMelee(perception)
val skeletonAI = BehaviorTemplates.aggressiveRanged(perception)
val sheepAI = BehaviorTemplates.passiveWanderer(patrolWaypoints)
```

### 扩展支付渠道

```kotlin
@Extension(id = "payment-wechat")
class WechatPaymentPlugin : AzathothPlugin() {

    override suspend fun onLoad() {
        registerService(PaymentProvider::class, WechatPaymentProvider())
    }
}

class WechatPaymentProvider : PaymentProvider {
    override val providerId = "wechat"
    override val displayName = "微信支付"

    override suspend fun createOrder(request: PaymentRequest): PaymentOrder {
        // 调用微信支付 API
        val result = wechatClient.unifiedOrder(
            outTradeNo = request.orderId,
            totalFee = request.amount.toCents(),
            body = request.description
        )
        return PaymentOrder(
            orderId = request.orderId,
            payUrl = result.codeUrl
        )
    }
}
```

---

## 项目结构

```
azathoth/
├── core/                        # 核心模块
│   ├── protocol/                # MC 协议定义
│   ├── common/                  # 公共工具库
│   ├── grpc-api/                # gRPC Proto 定义
│   └── kafka-events/            # Kafka 事件定义
│
├── gateway/                     # 网关服务
│   └── src/                     # 连接管理、认证、路由
│
├── game-instance/               # 游戏实例
│   ├── engine/                  # Minestom 扩展引擎
│   ├── mechanics/               # 战斗、技能、AI 行为树、感知、仇恨、寻路
│   └── dungeons/                # 副本逻辑
│
├── services/                    # 后端微服务
│   ├── player-service/          # 玩家数据服务
│   ├── chat-service/            # 聊天服务
│   ├── dungeon-service/         # 副本匹配与管理
│   ├── activity-service/        # 活动与任务
│   ├── guild-service/           # 公会系统
│   ├── trade-service/           # 交易系统
│   ├── mail-service/            # 邮件系统
│   ├── rollback-service/        # 玩家数据快照与回档 (ClickHouse)
│   └── admin-service/           # 管理后台 API
│
├── sdk/                         # 开发者 SDK
│   ├── azathoth-api/            # 核心 API
│   ├── azathoth-plugin-api/     # 插件开发 API
│   ├── azathoth-testing/        # 测试工具
│   └── azathoth-gradle-plugin/  # Gradle 构建插件
│
├── admin-frontend/              # 管理后台前端 (Vue 3)
├── client-mod/                  # Fabric 客户端模组
├── website/                     # 官网 (Nuxt 3)
│
└── deploy/                      # 部署配置
    ├── docker/                  # Docker Compose 与 Dockerfile
    ├── kubernetes/              # K8s 清单
    ├── agones/                  # Agones Fleet 配置
    └── helm/                    # Helm Charts
```

---

## 文档

| 文档 | 说明 |
|------|------|
| [快速入门](https://docs.mcwar.cn/getting-started) | 15 分钟上手 Azathoth |
| [架构设计](https://docs.mcwar.cn/architecture) | 深入理解系统架构 |
| [插件开发](https://docs.mcwar.cn/plugin-development) | 从零创建游戏插件 |
| [API 参考](https://docs.mcwar.cn/api) | 完整 API 文档 |
| [部署指南](https://docs.mcwar.cn/deployment) | 生产环境部署 |
| [最佳实践](https://docs.mcwar.cn/best-practices) | 性能优化与设计模式 |

---

## 贡献指南

我们欢迎所有形式的贡献！无论是报告 Bug、提出建议、改进文档还是贡献代码。

### 开发流程

```bash
# 1. Fork 并克隆仓库
git clone https://github.com/YOUR_USERNAME/azathoth.git
cd azathoth

# 2. 添加上游仓库
git remote add upstream https://github.com/MinecraftAzathoth/azathoth.git

# 3. 创建特性分支
git checkout -b feature/amazing-feature

# 4. 进行开发...

# 5. 运行测试确保通过
./gradlew test

# 6. 提交更改 (遵循 Conventional Commits)
git commit -m "feat(skills): add fireball skill implementation"

# 7. 推送并创建 Pull Request
git push origin feature/amazing-feature
```

### 提交规范

我们使用 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

| 类型 | 说明 |
|------|------|
| `feat` | 新功能 |
| `fix` | Bug 修复 |
| `docs` | 文档更新 |
| `style` | 代码格式调整 |
| `refactor` | 重构 |
| `perf` | 性能优化 |
| `test` | 测试相关 |
| `chore` | 构建/工具链 |

示例：
```
feat(dungeon): add dragon lair dungeon template
fix(gateway): resolve connection timeout issue
docs(readme): update installation instructions
```

### 代码规范

- 遵循 [Kotlin 官方编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 公共 API 必须编写 KDoc 注释
- 新功能必须包含单元测试
- 禁止滥用 `!!`，优先使用 Kotlin 空安全、显式判空和结果对象
- 数据库与 Redis 的 I/O 必须异步处理

### 测试命令

```bash
# 运行全部测试
./gradlew test

# 运行特定模块测试
./gradlew :game-instance:test
./gradlew :gateway:test
./gradlew :services:player-service:test
```

### 分支策略

| 分支 | 用途 |
|------|------|
| `main` | 稳定版本，受保护 |
| `develop` | 开发分支，PR 目标分支 |
| `feature/*` | 新功能开发 |
| `fix/*` | Bug 修复 |
| `release/*` | 版本发布准备 |

### 问题反馈

- **Bug 报告**: [GitHub Issues](https://github.com/MinecraftAzathoth/azathoth/issues/new?template=bug_report.md)
- **功能建议**: [GitHub Discussions](https://github.com/MinecraftAzathoth/azathoth/discussions/categories/ideas)
- **安全漏洞**: 请发送邮件至 security@mcwar.cn

---

## 路线图

### v0.1.0 - 基础框架 ✅
- [x] 项目结构搭建
- [x] 核心 API 设计
- [x] Gateway 基础功能
- [x] Game Instance 基础功能
- [x] 插件系统框架

### v0.2.0 - 核心游戏 ✅
- [x] 战斗系统
- [x] 技能系统
- [x] 副本系统
- [x] AI 行为树

### v0.3.0 - 社交与经济 ✅
- [x] 公会系统
- [x] 交易系统
- [x] 聊天系统
- [x] 邮件系统

### v0.4.0 - 运营工具 (当前)
- [x] 管理后台
- [x] 数据分析
- [x] 活动系统
- [x] 客户端模组
- [x] 玩家数据快照与回档系统 (ClickHouse)

### v1.0.0 - 正式发布
- [ ] 完整文档
- [ ] 性能优化
- [ ] 安全审计
- [ ] 开发者市场

---

## 许可证

本项目采用 **Azathoth Non-Commercial License (ANCL)** 许可证。

### 允许
- 个人学习、研究、教育用途
- 非商业目的的修改和使用
- 非商业目的的再分发（必须保留相同许可证）

### 禁止
- 任何形式的商业用途
- 使用本项目运营盈利性服务器
- 销售基于本项目的产品或服务

### 商业授权

如需商业使用，请联系我们获取商业授权：

- **邮箱**: business@mcwar.cn
- **官网**: https://www.mcwar.cn/license

完整许可证文本请查看 [LICENSE](LICENSE) 文件。

---

## 社区与支持

<div align="center">

[![QQ群](https://img.shields.io/badge/QQ群-1036836025-blue?style=flat-square&logo=tencentqq)](https://qm.qq.com/q/1036836025)
[![Discord](https://img.shields.io/badge/Discord-Azathoth-5865F2?style=flat-square&logo=discord&logoColor=white)](https://discord.gg/azathoth)
[![Bilibili](https://img.shields.io/badge/Bilibili-关注我们-00A1D6?style=flat-square&logo=bilibili&logoColor=white)](https://space.bilibili.com/xxx)

</div>

- **官方网站**: [www.mcwar.cn](https://www.mcwar.cn)
- **开发文档**: [docs.mcwar.cn](https://docs.mcwar.cn)
- **问题反馈**: [GitHub Issues](https://github.com/MinecraftAzathoth/azathoth/issues)
- **功能讨论**: [GitHub Discussions](https://github.com/MinecraftAzathoth/azathoth/discussions)

---

## 致谢

感谢以下开源项目，没有它们就没有 Azathoth：

- [Minestom](https://minestom.net/) - 高性能 Minecraft 服务器库
- [Kotlin](https://kotlinlang.org/) - 现代化编程语言
- [Agones](https://agones.dev/) - 游戏服务器编排
- [gRPC](https://grpc.io/) - 高性能 RPC 框架

---

<div align="center">

**Azathoth** - 为下一代 Minecraft MMORPG 而生

Made with passion by the Azathoth Team

</div>
