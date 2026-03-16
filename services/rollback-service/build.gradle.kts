plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
}

description = "Azathoth Services - Player Data Snapshot & Rollback (ClickHouse)"

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:grpc-api"))
    implementation(project(":core:kafka-events"))

    // ClickHouse JDBC
    implementation(libs.clickhouse.jdbc)
    implementation(libs.hikari)

    // Ktor for REST API
    implementation(libs.bundles.ktor.server)

    // Coroutines
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.jdk8)

    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.clickhouse)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = "com.azathoth.services.rollback.RollbackServiceMainKt"
    }
}
