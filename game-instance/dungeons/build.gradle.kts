plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Game Instance - Dungeon System (Instances, Bosses, Rewards)"

dependencies {
    api(project(":core:common"))
    api(project(":game-instance:engine"))
    api(project(":game-instance:mechanics"))

    // Minestom core (requires JVM 25 runtime, enable when available)
    // implementation(libs.minestom)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)

    // Testing
    testImplementation(libs.kotlin.coroutines.test)
}
