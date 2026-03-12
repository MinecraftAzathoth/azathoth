plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Core - Minecraft Protocol Implementation"

dependencies {
    api(project(":core:common"))
    api(libs.kotlin.serialization.json)
    api(libs.bundles.logging)

    testImplementation(libs.bundles.testing)
}
