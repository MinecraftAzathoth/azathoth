<div align="center">

# Azathoth

[![GitHub stars](https://img.shields.io/github/stars/MinecraftAzathoth/azathoth?style=flat-square&logo=github)](https://github.com/MinecraftAzathoth/azathoth/stargazers)
[![Website](https://img.shields.io/badge/Website-mcwar.cn-FF6B6B?style=flat-square&logo=globe&logoColor=white)](https://www.mcwar.cn)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Minestom](https://img.shields.io/badge/Minestom-latest-00AA00?style=flat-square)](https://minestom.net/)
[![License](https://img.shields.io/badge/License-ANCL-red.svg?style=flat-square)](LICENSE)

**MMORPG-Grade Minecraft Server Framework**

*Built for the next generation of massively multiplayer online games, supporting hundreds of thousands of concurrent players*

English | [简体中文](README.md)

[Website](https://www.mcwar.cn) · [Documentation](https://docs.mcwar.cn) · [Report Issues](https://github.com/MinecraftAzathoth/azathoth/issues)

</div>

---

## Vision

Imagine building a true MMORPG within Minecraft—epic dungeons, guild wars, real-time economy systems, with hundreds of thousands of players adventuring in the same universe simultaneously.

**Azathoth makes this possible.**

We're not just another server framework. Azathoth is a complete game infrastructure solution, from low-level protocols to cloud deployment, from plugin development to operations management, providing everything developers need to build massive online games.

```
┌─────────────────────────────────────────────────────────────────┐
│                      Players (Fabric Client)                     │
└─────────────────────────┬───────────────────────────────────────┘
                          │ MC Protocol
┌─────────────────────────▼───────────────────────────────────────┐
│                      Gateway Service                             │
│        Connection Mgmt · Smart Routing · Seamless Transfer       │
└─────────────────────────┬───────────────────────────────────────┘
                          │ gRPC
┌─────────────────────────▼───────────────────────────────────────┐
│                    Game Instance Layer                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐         │
│  │   Hub    │  │ Dungeon  │  │Battlegrnd│  │   ...    │         │
│  │(Minestom)│  │(Minestom)│  │(Minestom)│  │          │         │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘         │
│                    Managed by Agones Fleet                       │
└──────────────────────┬──────────────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         │ gRPC (Sync)    Kafka (Async)│
         ▼                           ▼
┌─────────────────┐    ┌──────────────────────────────────────────┐
│ Backend Services│    │              Message Bus                  │
│ ├─ Player       │    │   Cross-server Broadcast · Event-Driven  │
│ ├─ Dungeon      │    └──────────────────────────────────────────┘
│ ├─ Guild        │
│ └─ ...          │
└─────────────────┘
```

---

## Features

### Built for Scale

<table>
<tr>
<td width="50%">

#### Hundreds of Thousands Online
Designed with microservices architecture where each component scales independently. Gateway handles connections, Game Instance runs game logic, Backend Services manage business data—three-layer decoupling with linear scalability.

</td>
<td width="50%">

#### High-Performance Minestom Core
Leaving behind Bukkit's legacy, built from scratch with Minestom. No tick limitations, pure async design, single instance supports thousands of players. Coroutine-first API makes concurrent programming feel like writing synchronous code.

</td>
</tr>
<tr>
<td width="50%">

#### Agones Auto-Scaling
Game instances orchestrated by Kubernetes + Agones. Auto-scale up when players flood in, scale down when idle. Buffer pools ensure new instances are instantly available—goodbye manual ops.

</td>
<td width="50%">

#### Hot-Reload Plugin System
Three plugin types for different needs: Core (cold-load fundamentals), Game (hot-load game content), Extension (hot-load services). Modify skill parameters? Update dungeon config? No restart needed, instant effect.

</td>
</tr>
<tr>
<td width="50%">

#### gRPC + Kafka Hybrid Communication
Use gRPC for sync calls—low latency, strong typing, bidirectional streaming. Use Kafka for async messages—high throughput, persistence, perfect decoupling. Choose the optimal approach per scenario.

</td>
<td width="50%">

#### Complete Admin Dashboard
Modern management system built with Vue 3 + Ktor. Real-time server monitoring, one-click player management, visual game content configuration. RBAC permission model for secure control.

</td>
</tr>
<tr>
<td colspan="2">

#### Developer Marketplace Ecosystem
More than a framework—it's an ecosystem. Official marketplace supports plugin publishing, trading, and reviews. Free open-source or paid licensing, developers choose freely. Comprehensive SDK and bilingual documentation lower the entry barrier.

</td>
</tr>
</table>

---

## Tech Stack

| Category | Technology | Version | Description |
|----------|------------|---------|-------------|
| **Language** | Kotlin | 2.3.0 | Primary language, coroutine-first |
| **Language** | Java | 25 | Underlying compatibility |
| **Build** | Gradle (Kotlin DSL) | 9.2.1 | Multi-module builds |
| **Game Core** | Minestom | latest | High-performance MC server library |
| **Communication** | gRPC-Kotlin | 1.5.0 | Inter-service sync calls |
| **Message Queue** | Kafka | 3.6+ | Async messaging |
| **Backend Framework** | Ktor | 3.3.3 | Admin API |
| **Frontend Framework** | Vue 3 | 3.5+ | Admin UI |
| **Orchestration** | Kubernetes + Agones | - | Container orchestration & game server management |
| **Database** | PostgreSQL | 16+ | Structured data storage |
| **Database** | MongoDB | 7+ | Document data storage |
| **Cache** | Redis | 7+ | Sessions, cache, leaderboards |
| **Analytics** | ClickHouse | - | Behavior logs & analytics |

---

## Quick Start

### Requirements

- **JDK 25+** (recommended: [Eclipse Temurin](https://adoptium.net/))
- **Kotlin 2.3.0+**
- **Docker & Docker Compose**
- **Gradle 9.2.1+** (Wrapper included)

### Local Development Environment

```bash
# 1. Clone the repository
git clone https://github.com/MinecraftAzathoth/azathoth.git
cd azathoth

# 2. Start infrastructure services
docker-compose -f deploy/docker-compose.dev.yml up -d

# 3. Build the project
./gradlew build

# 4. Run Gateway service
./gradlew :gateway:run

# 5. Run Game Instance (new terminal)
./gradlew :game-instance:run
```

### Plugin Development

```bash
# Use the project generator to create a plugin project (coming soon)
# Visit https://www.mcwar.cn/generator

# Or create manually
mkdir my-plugin && cd my-plugin
./gradlew init --type kotlin-library
```

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

### Cluster Deployment

```bash
# 1. Configure Kubernetes cluster (Agones required)
kubectl apply -f deploy/agones/

# 2. Deploy infrastructure
helm install azathoth-infra deploy/helm/infrastructure/

# 3. Deploy game services
helm install azathoth deploy/helm/azathoth/

# 4. Check status
kubectl get fleet -n azathoth
```

See [Deployment Guide](https://docs.mcwar.cn/deployment) for detailed instructions.

---

## Examples

### Creating a Skill Plugin

```kotlin
@Skill(id = "fireball", name = "Fireball")
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

        // Play sounds and particles
        context.playSound(Sound.ENTITY_BLAZE_SHOOT)
        context.spawnParticles(Particle.FLAME, count = 20)

        return SkillResult.success()
    }
}
```

### Creating a Dungeon

```kotlin
@Dungeon(id = "dragon_lair", name = "Dragon's Lair")
class DragonLairDungeon : AbstractDungeon() {

    override val minPlayers = 5
    override val maxPlayers = 10
    override val timeLimit = 30.minutes

    override suspend fun onSetup(context: DungeonContext) {
        // Load map
        context.loadSchematic("dungeons/dragon_lair.schem")

        // Register phases
        registerPhase(TrashMobPhase())
        registerPhase(MiniBossPhase())
        registerPhase(FinalBossPhase())
    }

    inner class FinalBossPhase : DungeonPhase("final_boss") {
        override suspend fun onStart(context: PhaseContext) {
            context.announce("The dragon awakens!")

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

### Creating an AI Behavior Tree

```kotlin
@BehaviorTree(id = "dragon_ai")
class DragonAI : AbstractBehaviorTree() {

    override fun build(): BehaviorNode = selector {
        // Flee when low health
        sequence {
            condition { entity.healthPercent < 0.2 }
            action { flee(duration = 5.seconds) }
            action { castSkill("heal") }
        }

        // Ranged attack
        sequence {
            condition { distanceToTarget > 10 }
            action { castSkill("fire_breath") }
        }

        // Melee attack
        sequence {
            condition { distanceToTarget <= 3 }
            action { meleeAttack() }
        }

        // Approach target
        action { moveTo(target.position) }
    }
}
```

### Extending Payment Channels

```kotlin
@Extension(id = "payment-stripe")
class StripePaymentPlugin : AzathothPlugin() {

    override suspend fun onLoad() {
        registerService(PaymentProvider::class, StripePaymentProvider())
    }
}

class StripePaymentProvider : PaymentProvider {
    override val providerId = "stripe"
    override val displayName = "Stripe"

    override suspend fun createOrder(request: PaymentRequest): PaymentOrder {
        // Call Stripe API
        val session = stripeClient.checkout.sessions.create(
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .addLineItem(...)
                .build()
        )
        return PaymentOrder(
            orderId = request.orderId,
            payUrl = session.url
        )
    }
}
```

---

## Project Structure

```
azathoth/
├── core/                        # Core modules
│   ├── protocol/                # MC protocol definitions
│   ├── common/                  # Common utilities
│   ├── grpc-api/                # gRPC Proto definitions
│   └── kafka-events/            # Kafka event definitions
│
├── gateway/                     # Gateway service
│   └── src/                     # Connection mgmt, auth, routing
│
├── game-instance/               # Game instance
│   ├── engine/                  # Minestom extensions
│   ├── mechanics/               # Combat, skills, AI systems
│   └── dungeons/                # Dungeon logic
│
├── services/                    # Backend microservices
│   ├── player-service/          # Player data service
│   ├── chat-service/            # Chat service
│   ├── dungeon-service/         # Dungeon matching & management
│   ├── activity-service/        # Activities & quests
│   ├── guild-service/           # Guild system
│   ├── trade-service/           # Trading system
│   └── admin-service/           # Admin API
│
├── sdk/                         # Developer SDK
│   ├── azathoth-api/            # Core API
│   ├── azathoth-plugin-api/     # Plugin development API
│   └── azathoth-testing/        # Testing utilities
│
├── admin-frontend/              # Admin frontend (Vue 3)
├── client-mod/                  # Fabric client mod
├── website/                     # Website (Nuxt 3)
│
└── deploy/                      # Deployment configs
    ├── kubernetes/              # K8s manifests
    ├── agones/                  # Agones Fleet configs
    └── helm/                    # Helm Charts
```

---

## Documentation

| Document | Description |
|----------|-------------|
| [Quick Start](https://docs.mcwar.cn/en/getting-started) | Get started with Azathoth in 15 minutes |
| [Architecture](https://docs.mcwar.cn/en/architecture) | Deep dive into system architecture |
| [Plugin Development](https://docs.mcwar.cn/en/plugin-development) | Create game plugins from scratch |
| [API Reference](https://docs.mcwar.cn/en/api) | Complete API documentation |
| [Deployment Guide](https://docs.mcwar.cn/en/deployment) | Production deployment |
| [Best Practices](https://docs.mcwar.cn/en/best-practices) | Performance optimization & design patterns |

---

## Contributing

We welcome all forms of contributions! Whether it's reporting bugs, suggesting features, improving documentation, or contributing code.

### Development Workflow

```bash
# 1. Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/azathoth.git
cd azathoth

# 2. Add upstream remote
git remote add upstream https://github.com/MinecraftAzathoth/azathoth.git

# 3. Create a feature branch
git checkout -b feature/amazing-feature

# 4. Develop...

# 5. Run tests to ensure they pass
./gradlew test
./gradlew ktlintCheck

# 6. Commit changes (follow Conventional Commits)
git commit -m "feat(skills): add fireball skill implementation"

# 7. Push and create Pull Request
git push origin feature/amazing-feature
```

### Commit Convention

We use [Conventional Commits](https://www.conventionalcommits.org/):

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation update |
| `style` | Code formatting |
| `refactor` | Refactoring |
| `perf` | Performance improvement |
| `test` | Testing related |
| `chore` | Build/tooling |

Examples:
```
feat(dungeon): add dragon lair dungeon template
fix(gateway): resolve connection timeout issue
docs(readme): update installation instructions
```

### Code Standards

- Follow [Kotlin Official Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `ktlint` for code formatting: `./gradlew ktlintFormat`
- Public APIs must have KDoc comments
- New features must include unit tests
- Maintain test coverage above 80%

### Test Commands

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :game-instance:test

# Run integration tests (requires Docker)
./gradlew integrationTest

# Generate test coverage report
./gradlew jacocoTestReport
```

### Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Stable release, protected |
| `develop` | Development branch, PR target |
| `feature/*` | New feature development |
| `fix/*` | Bug fixes |
| `release/*` | Release preparation |

### Issue Reporting

- **Bug Reports**: [GitHub Issues](https://github.com/MinecraftAzathoth/azathoth/issues/new?template=bug_report.md)
- **Feature Requests**: [GitHub Discussions](https://github.com/MinecraftAzathoth/azathoth/discussions/categories/ideas)
- **Security Vulnerabilities**: Please email security@mcwar.cn

---

## Roadmap

### v0.1.0 - Foundation (Current)
- [x] Project structure setup
- [x] Core API design
- [ ] Gateway basic functionality
- [ ] Game Instance basic functionality
- [ ] Plugin system framework

### v0.2.0 - Core Gameplay
- [ ] Combat system
- [ ] Skill system
- [ ] Dungeon system
- [ ] AI behavior trees

### v0.3.0 - Social & Economy
- [ ] Guild system
- [ ] Trading system
- [ ] Chat system
- [ ] Mail system

### v0.4.0 - Operations Tools
- [ ] Admin dashboard
- [ ] Data analytics
- [ ] Activity system
- [ ] Client mod

### v1.0.0 - Official Release
- [ ] Complete documentation
- [ ] Performance optimization
- [ ] Security audit
- [ ] Developer marketplace

---

## License

This project is licensed under the **Azathoth Non-Commercial License (ANCL)**.

### Permitted
- Personal learning, research, and educational use
- Non-commercial modification and use
- Non-commercial redistribution (must retain same license)

### Prohibited
- Any form of commercial use
- Operating profit-generating servers using this project
- Selling products or services based on this project

### Commercial Licensing

For commercial use, please contact us for a commercial license:

- **Email**: business@mcwar.cn
- **Website**: https://www.mcwar.cn/license

See the full license text in the [LICENSE](LICENSE) file.

---

## Community & Support

<div align="center">

[![Discord](https://img.shields.io/badge/Discord-Azathoth-5865F2?style=flat-square&logo=discord&logoColor=white)](https://discord.gg/azathoth)
[![Twitter](https://img.shields.io/badge/Twitter-@AzathothMC-1DA1F2?style=flat-square&logo=twitter&logoColor=white)](https://twitter.com/AzathothMC)

</div>

- **Website**: [www.mcwar.cn](https://www.mcwar.cn)
- **Documentation**: [docs.mcwar.cn](https://docs.mcwar.cn)
- **Issue Tracker**: [GitHub Issues](https://github.com/MinecraftAzathoth/azathoth/issues)
- **Discussions**: [GitHub Discussions](https://github.com/MinecraftAzathoth/azathoth/discussions)

---

## Acknowledgements

Thanks to these open-source projects, without which Azathoth wouldn't exist:

- [Minestom](https://minestom.net/) - High-performance Minecraft server library
- [Kotlin](https://kotlinlang.org/) - Modern programming language
- [Agones](https://agones.dev/) - Game server orchestration
- [gRPC](https://grpc.io/) - High-performance RPC framework

---

<div align="center">

**Azathoth** - Built for the Next Generation of Minecraft MMORPGs

Made with passion by the Azathoth Team

</div>
