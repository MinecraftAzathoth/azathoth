plugins {
    alias(libs.plugins.kotlin.serialization)
}

description = "Azathoth Core - Kafka Event Definitions"

dependencies {
    api(project(":core:common"))
    api(libs.kafka.clients)
    api(libs.kotlin.serialization.json)
}
