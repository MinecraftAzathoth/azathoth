plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

description = "Azathoth SDK - Gradle Plugin for Plugin Development"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.serialization.json)

    // Gradle API is provided by the java-gradle-plugin
}

gradlePlugin {
    plugins {
        create("azathoth") {
            id = "com.azathoth.plugin"
            implementationClass = "com.azathoth.gradle.AzathothPlugin"
            displayName = "Azathoth Plugin"
            description = "Gradle plugin for developing Azathoth plugins"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.azathoth"
            artifactId = "azathoth-gradle-plugin"
            version = project.version.toString()
        }
    }
}
