plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

description = "Azathoth Gateway - Player Connection Management and Load Balancing"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:protocol"))
    implementation(project(":core:grpc-api"))
    implementation(project(":core:kafka-events"))

    // Netty for network handling
    implementation(libs.minestom)

    // gRPC for service communication
    implementation(libs.bundles.grpc)

    // Kafka for event streaming
    implementation(libs.kafka.clients)

    // Redis for session management
    implementation(libs.lettuce)

    // Ktor for HTTP API
    implementation(libs.bundles.ktor.server)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.jdk8)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.azathoth.gateway.GatewayMainKt"
    }
}
