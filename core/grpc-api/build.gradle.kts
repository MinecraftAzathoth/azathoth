plugins {
    alias(libs.plugins.protobuf)
}

description = "Azathoth Core - gRPC API Definitions"

// 从版本目录中提取版本号
val protobufVersion: String = libs.versions.protobuf.asProvider().get()
val grpcVersion: String = libs.versions.grpc.asProvider().get()
val grpcKotlinVersion: String = libs.versions.grpc.kotlin.get()

dependencies {
    api(project(":core:common"))
    api(libs.bundles.grpc)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}
