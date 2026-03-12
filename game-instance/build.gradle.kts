plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

description = "Azathoth Game Instance - Main Game Server Application"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:protocol"))
    implementation(project(":core:grpc-api"))
    implementation(project(":core:kafka-events"))
    implementation(project(":game-instance:engine"))
    implementation(project(":game-instance:mechanics"))
    implementation(project(":game-instance:dungeons"))

    // Minestom core (requires JVM 25+, comment out for JVM 21 development)
    // implementation(libs.minestom)

    // gRPC for service communication
    implementation(libs.bundles.grpc)

    // Kafka for event streaming
    implementation(libs.kafka.clients)

    // Redis for caching
    implementation(libs.lettuce)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.jdk8)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.azathoth.game.GameInstanceMainKt"
    }
}
