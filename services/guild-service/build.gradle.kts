plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

description = "Azathoth Services - Guild Management System"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:grpc-api"))
    implementation(project(":core:kafka-events"))

    // gRPC server
    implementation(libs.bundles.grpc)

    // Kafka
    implementation(libs.kafka.clients)

    // Database
    implementation(libs.bundles.database)

    // Redis for caching
    implementation(libs.lettuce)

    // Ktor for HTTP API
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
        attributes["Main-Class"] = "com.azathoth.services.guild.GuildServiceMainKt"
    }
}
