plugins {
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

description = "Azathoth SDK - Core API for Plugin Development"

dependencies {
    api(project(":core:common"))
    api(project(":core:protocol"))

    // Minestom API (compileOnly - provided at runtime)
    // compileOnly(libs.minestom) // Disabled: Minestom requires JVM 25, SDK API doesn't need it directly

    // Kotlin Serialization
    api(libs.kotlin.serialization.json)

    // Coroutines
    api(libs.kotlin.coroutines.core)

    // Kotlin Reflect (for annotation-based registration)
    implementation(libs.kotlin.reflect)

    // Logging
    implementation(libs.kotlin.logging)

    // Testing
    testImplementation(libs.kotlin.coroutines.test)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.azathoth"
            artifactId = "azathoth-api"
            version = project.version.toString()
            from(components["kotlin"])
        }
    }
}
