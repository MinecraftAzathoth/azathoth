plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

description = "Azathoth Services - Admin Backend and Management API"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:grpc-api"))
    implementation(project(":core:kafka-events"))

    // gRPC client for other services
    implementation(libs.bundles.grpc)

    // Kafka for logs and analytics
    implementation(libs.kafka.clients)
    implementation(libs.kafka.streams)

    // Database
    implementation(libs.bundles.database)
    implementation(libs.mongodb.driver)

    // Redis
    implementation(libs.lettuce)

    // Ktor for REST API
    implementation(libs.bundles.ktor.server)

    // Ktor client for external calls
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.jdk8)
    implementation(libs.kotlin.coroutines.reactive)

    testImplementation(libs.kotlin.coroutines.test)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.azathoth.services.admin.AdminServiceMainKt"
    }
}
