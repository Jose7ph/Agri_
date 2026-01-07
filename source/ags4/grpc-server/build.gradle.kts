
plugins {
//    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    application
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

dependencies {
    // later:
    // implementation(project(":grpc-proto"))

    implementation("io.grpc:grpc-netty-shaded:1.65.1")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")
    implementation("io.grpc:grpc-protobuf:1.65.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.6")
}


application {
    mainClass = "com.example.grpc_server.MainKt"
}
