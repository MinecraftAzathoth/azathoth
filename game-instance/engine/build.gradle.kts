plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Game Instance - Core Engine (World, Entity, Physics)"

dependencies {
    api(project(":core:common"))
    api(project(":core:protocol"))

    // Minestom core (requires JVM 25 runtime, enable when available)
    // api(libs.minestom)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)

    // Testing
    testImplementation(libs.kotlin.coroutines.test)
}
