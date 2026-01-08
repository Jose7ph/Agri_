import com.google.protobuf.gradle.*

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("com.google.protobuf") version "0.9.5"
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}


val grpcVersion = "1.75.0"
val protobufVersion = "4.28.2"

dependencies {
    // LITE runtime (best for Android + works on server too)
    api("io.grpc:grpc-protobuf-lite:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")
    api("com.google.protobuf:protobuf-javalite:$protobufVersion")

    // Needed by generated code annotations sometimes
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }

    generateProtoTasks {
        ofSourceSet("main").forEach { task ->
            // Configure existing builtin "java" (do NOT add it again)
            task.builtins {
                named("java") {
                    option("lite")
                }
            }

            // Add grpc plugin + configure it
            task.plugins {
                id("grpc") {
                    option("lite")
                }
            }
        }
    }
}
