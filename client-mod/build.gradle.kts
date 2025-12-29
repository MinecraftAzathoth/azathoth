// Note: This module uses Fabric Loom for Minecraft mod development
// The actual Fabric setup requires additional configuration
// This is a placeholder showing the intended structure

plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Client Mod - Fabric Mod for Enhanced Client Features"

// Note: Fabric Loom plugin needs to be added to settings.gradle.kts
// id("fabric-loom") version "1.9-SNAPSHOT"

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.serialization.json)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)

    // Note: Fabric dependencies would be added via Loom:
    // minecraft("com.mojang:minecraft:1.21.4")
    // mappings("net.fabricmc:yarn:1.21.4+build.1:v2")
    // modImplementation("net.fabricmc:fabric-loader:0.16.9")
    // modImplementation("net.fabricmc.fabric-api:fabric-api:0.110.5+1.21.4")
    // modImplementation("net.fabricmc:fabric-language-kotlin:1.13.0+kotlin.2.1.0")
}

// Fabric Loom tasks would be configured here
// processResources { ... }
// tasks.jar { ... }
