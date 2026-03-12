plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Game Instance - Game Mechanics (Combat, Skills, Items)"

dependencies {
    api(project(":core:common"))
    api(project(":game-instance:engine"))

    // Minestom core (requires JVM 25 runtime, enable when available)
    // implementation(libs.minestom)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)

    // Testing
    testImplementation(libs.kotlin.coroutines.test)
}
