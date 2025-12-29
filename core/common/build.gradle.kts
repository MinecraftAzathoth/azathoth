plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Core - Common Utilities and Shared Code"

dependencies {
    api(libs.kotlin.serialization.json)
    api(libs.bundles.logging)
    api(libs.guava)
    api(libs.caffeine)
}
