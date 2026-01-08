plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("application")
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
val grpcKotlinVersion = "1.4.1"
val coroutinesVersion = "1.8.1"

dependencies {
    implementation(project(":grpc-proto"))

    // Server transport (JVM)
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")

    // Only if your server code uses coroutine stubs / grpc-kotlin APIs
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
}


application {
    mainClass = "com.example.drone.server.DroneServerKt"
}
