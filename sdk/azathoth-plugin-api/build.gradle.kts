plugins {
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

description = "Azathoth SDK - Plugin Development API"

dependencies {
    api(project(":sdk:azathoth-api"))

    // Minestom API (compileOnly - provided at runtime)
    // compileOnly(libs.minestom) // Disabled: Minestom requires JVM 25

    // Kotlin Serialization
    api(libs.kotlin.serialization.json)

    // Coroutines
    api(libs.kotlin.coroutines.core)

    // Logging
    api(libs.kotlin.logging)

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.mockk)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.azathoth"
            artifactId = "azathoth-plugin-api"
            version = project.version.toString()
            from(components["kotlin"])
        }
    }
}
