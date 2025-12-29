plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

description = "Azathoth Website - Backend API Server (Ktor)"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:grpc-api"))

    // Ktor Server
    implementation(libs.bundles.ktor.server)

    // Ktor Client (for external API calls)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)

    // gRPC client for internal services
    implementation(libs.bundles.grpc)

    // Database
    implementation(libs.bundles.database)
    implementation(libs.mongodb.driver)

    // Redis for sessions and caching
    implementation(libs.lettuce)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.jdk8)
    implementation(libs.kotlin.coroutines.reactive)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.azathoth.website.WebsiteMainKt"
    }
}
