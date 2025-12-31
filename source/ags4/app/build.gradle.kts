import com.android.build.gradle.internal.api.ApkVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.googleKsp)
    alias(libs.plugins.googleAndroidMapsPlugin)
}

android {
    namespace = "com.jiagu.ags4"
    compileSdk = 35

    signingConfigs {
//        getByName("debug") {
//            storeFile = file("******")
//            storePassword = "******"
//            keyAlias = "******"
//            keyPassword = "******"
//        }
    }

    defaultConfig {
        applicationId = "com.baibars.ags4"
        minSdk = 25
        targetSdk = 34
        versionCode = 224
        versionName = "4.0.25-${versionCode}"
        signingConfig = signingConfigs.getByName("debug")
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
//            abiFilters.add("arm64-v8a") //"armeabi-v7a"
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }
        resourceConfigurations.addAll(
            listOf("zh-rCN")
        )
        // ags4-vk ags4-beta ags4-ko ags4-th
        buildConfigField("String", "FW_PREFIX", "\"ags4-vk\"")
        buildConfigField("String", "DEFAULT_PRIMARY_COLOR", "0xFF07913A")
        buildConfigField("String", "SERVER_URL", "\"http://ag.jiagutech.com/api/\"")
        buildConfigField("Boolean", "CLEAN", "false")
        buildConfigField("String", "SMS_SENDER", "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    flavorDimensions.add("default")

    productFlavors {
        // 微克
        create("vk") {
            dimension = "default"
            versionName = "vk-" + defaultConfig.versionName
            manifestPlaceholders["appLabel"] = "@string/ags4_name"
            buildConfigField("int", "DEFAULT_PRIMARY_COLOR", "0xFF07913A")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = true
    }
    applicationVariants.all {
        if (buildType.name == "release") {
            outputs.all { output ->
                if (output is ApkVariantOutputImpl) {
                    output.outputFileName = "ags4-${versionName}.apk"
                }
                true
            }
        }
    }

    secrets {
        // Optionally specify a different file name containing your secrets.
        // The plugin defaults to "local.properties"
        propertiesFileName = "secrets.properties"

        // A properties file containing default secret values. This file can be
        // checked in version control.
        defaultPropertiesFileName = "local.defaults.properties"

        // Configure which keys should be ignored by the plugin by providing regular expressions.
        // "sdk.dir" is ignored by default.
        ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
        ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
    }

    lint {
        abortOnError = false
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(fileTree("libs"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.androidx.paging.common.android)
    implementation(libs.androidx.paging.compose)
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.room.runtime)
    ksp(libs.room.ksp)
    implementation(libs.room.ktx)
    implementation(libs.easypermissions)
    implementation(libs.xcrash.android.lib)
    implementation(libs.greenrobot.eventbus)
    implementation(libs.coil.compose)
    implementation(libs.basepopup)

    // ExoPlayer Compose
    implementation(libs.androidx.media3.media3.exoplayer6)
    implementation(libs.androidx.media3.media3.ui4)

    implementation(libs.compose.markdown)
    implementation(libs.lfilepickerlibrary)
    implementation(libs.retrofit)
    implementation(libs.converterGson)
    implementation(libs.loggingInterceptor)
    implementation(libs.gson)
    implementation(libs.jts)
//    implementation(libs.mapbox)
    implementation(libs.googlemap)
    testImplementation(kotlin("test"))
//    implementation(project(":v9sdk"))

}