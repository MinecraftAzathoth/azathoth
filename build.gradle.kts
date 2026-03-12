import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.protobuf) apply false
}

allprojects {
    group = "com.azathoth"
    version = "1.0.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.addAll(
                "-Xjsr305=strict",
                "-Xcontext-receivers"
            )
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    dependencies {
        val implementation by configurations
        val testImplementation by configurations

        // Kotlin Standard Library
        implementation(rootProject.libs.kotlin.stdlib)
        implementation(rootProject.libs.kotlin.coroutines.core)

        // Testing
        testImplementation(rootProject.libs.kotlin.test)
        testImplementation(rootProject.libs.junit.jupiter)
        testImplementation(rootProject.libs.mockk)
    }
}
