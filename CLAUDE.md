# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Azathoth is an MMORPG-grade Minecraft server framework built on Minestom, designed for massive scale (hundreds of thousands of concurrent players). It uses a microservices architecture with gRPC for synchronous communication and Kafka for async messaging.

## Build Commands

```bash
# Build entire project
./gradlew build

# Build specific module
./gradlew :gateway:build
./gradlew :game-instance:build

# Run tests
./gradlew test
./gradlew :game-instance:test          # Single module

# Generate shadow JAR
./gradlew :gateway:shadowJar
./gradlew :game-instance:shadowJar

# Run services
./gradlew :gateway:run
./gradlew :game-instance:run
```

## Infrastructure (Docker)

```bash
# Start dev infrastructure (PostgreSQL, MongoDB, Redis, Kafka)
docker-compose -f deploy/docker/docker-compose.yml up -d

# Stop
docker-compose -f deploy/docker/docker-compose.yml down
```

Dev credentials: user `azathoth`, password `azathoth_dev`

## Admin Frontend

```bash
cd admin-frontend
npm install
npm run dev          # Dev server
npm run build        # Production build
npm run lint         # ESLint
npm run type-check   # TypeScript check
```

## Architecture

### Three-Layer Design

```
Players → Gateway → Game Instances → Backend Services
              ↓           ↓              ↓
           (gRPC)      (gRPC)        (Kafka)
```

- **Gateway** (`gateway/`): Connection management, authentication, load balancing, routing
- **Game Instance** (`game-instance/`): Minestom-based game logic, runs actual gameplay
- **Backend Services** (`services/`): Stateless microservices for data management

### Module Structure

**Core modules** (`core/`):
- `protocol/` - MC protocol definitions
- `common/` - Shared utilities (Configuration, Lifecycle, ServiceRegistry, Cache)
- `grpc-api/` - Proto definitions for 7 services (player, gateway, chat, guild, dungeon, trade, activity)
- `kafka-events/` - Event bus abstractions

**Game Instance** (`game-instance/`):
- `engine/` - Minestom extensions
- `mechanics/` - Combat, skills, AI systems
- `dungeons/` - Dungeon logic

**SDK** (`sdk/`):
- `azathoth-api/` - Core API for plugins
- `azathoth-plugin-api/` - Plugin development interfaces
- `azathoth-gradle-plugin/` - Build tooling for plugins

### Plugin System

Three plugin types:
- **Core**: Cold-loaded fundamentals
- **Game**: Hot-reloadable game content
- **Extension**: Hot-reloadable service extensions

### Communication Patterns

- **gRPC**: Synchronous service-to-service calls (defined in `core/grpc-api/src/main/proto/`)
- **Kafka**: Async events, cross-server broadcasts, data sync

## Tech Stack

- **Language**: Kotlin 2.3.0 (coroutine-first), Java 25
- **Game Core**: Minestom
- **Backend**: Ktor 3.3.3
- **Database**: PostgreSQL (Exposed ORM), MongoDB, Redis (Lettuce)
- **Messaging**: gRPC-Kotlin 1.5.0, Kafka 4.1.1
- **Orchestration**: Kubernetes + Agones
- **Frontend**: Vue 3 + Rspack

## Key Conventions

- Kotlin compiler flags: `-Xjsr305=strict`, `-Xcontext-receivers`
- JVM target: 25
- Tests use JUnit 5 Platform
- Version catalog at `gradle/libs.versions.toml`
- Type-safe project accessors enabled
