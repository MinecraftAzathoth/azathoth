plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Game Instance - Game Mechanics (Combat, Skills, Items)"

dependencies {
    api(project(":core:common"))
    api(project(":game-instance:engine"))

    // Minestom core
    implementation(libs.minestom)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
}
