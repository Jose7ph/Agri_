// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.googleKsp) apply false
    alias(libs.plugins.googleAndroidMapsPlugin) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

buildscript {
    extra["compile_sdk_version"] = 35
    extra["target_sdk_version"] = 31
    extra["lifecycle_version"] = "2.7.0"
    extra["paging_version"] = "3.1.1"
    extra["room_version"] = "2.5.2"
//        navigation_version = "2.7.7"
}

val compileSdk: Int by extra(35)
