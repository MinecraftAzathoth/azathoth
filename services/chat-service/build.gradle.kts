plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

description = "Azathoth Services - Chat and Messaging System"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:grpc-api"))
    implementation(project(":core:kafka-events"))

    // gRPC server
    implementation(libs.bundles.grpc)

    // Kafka for message streaming
    implementation(libs.kafka.clients)

    // Redis for pub/sub and caching
    implementation(libs.lettuce)

    // Database
    implementation(libs.mongodb.driver)

    // Ktor for WebSocket
    implementation(libs.bundles.ktor.server)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.jdk8)
    implementation(libs.kotlin.coroutines.reactive)

    testImplementation(libs.kotlin.coroutines.test)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.azathoth.services.chat.ChatServiceMainKt"
    }
}
