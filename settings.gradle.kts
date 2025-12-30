rootProject.name = "azathoth"

// Plugin Management
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// Dependency Resolution Management
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// ==================== Core Modules ====================
include(":core:protocol")
include(":core:common")
include(":core:grpc-api")
include(":core:kafka-events")

// ==================== Gateway ====================
include(":gateway")

// ==================== Game Instance ====================
include(":game-instance")
include(":game-instance:engine")
include(":game-instance:mechanics")
include(":game-instance:dungeons")

// ==================== Backend Services ====================
include(":services:player-service")
include(":services:chat-service")
include(":services:dungeon-service")
include(":services:activity-service")
include(":services:guild-service")
include(":services:trade-service")
include(":services:admin-service")

// ==================== SDK ====================
include(":sdk:azathoth-api")
include(":sdk:azathoth-plugin-api")
include(":sdk:azathoth-testing")
include(":sdk:azathoth-gradle-plugin")

// ==================== Client Mod ====================
include(":client-mod")

// Note: Website is deployed separately and not included in framework project
// Website project is located in website/ directory, requires separate build and deployment

// Enable Type-Safe Project Accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
