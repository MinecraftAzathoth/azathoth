plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Core - Common Utilities and Shared Code"

dependencies {
    api(libs.kotlin.serialization.json)
    api(libs.kotlin.serialization.protobuf)
    api(libs.bundles.logging)
    api(libs.guava)
    api(libs.caffeine)
    api(libs.kotlin.reflect)
    api(libs.kotlin.coroutines.core)

    // YAML support via Jackson
    api(libs.jackson.databind)
    api(libs.jackson.kotlin)
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:${libs.versions.jackson.get()}")

    testImplementation(libs.kotlin.coroutines.test)
}
