plugins {
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

description = "Azathoth SDK - Testing Utilities for Plugin Development"

dependencies {
    api(project(":sdk:azathoth-api"))
    api(project(":sdk:azathoth-plugin-api"))

    // Minestom for testing
    api(libs.minestom)

    // Testing frameworks
    api(libs.junit.jupiter)
    api(libs.junit.jupiter.api)
    api(libs.mockk)
    api(libs.kotlin.coroutines.test)

    // Testcontainers
    api(libs.testcontainers)
    api(libs.testcontainers.junit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.azathoth"
            artifactId = "azathoth-testing"
            version = project.version.toString()
            from(components["kotlin"])
        }
    }
}
