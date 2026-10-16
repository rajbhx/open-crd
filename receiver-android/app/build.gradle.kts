plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Signing is enabled only when CI materializes the keystore and exports
// ANDROID_KEYSTORE_BASE64 (see .github/workflows/release.yml). Local builds
// and debug builds never touch the release key.
val signingEnabled = System.getenv("ANDROID_KEYSTORE_BASE64") != null
val appVersionName = System.getenv("OPENCRD_VERSION") ?: "0.2.0"

android {
    namespace = "io.github.rajbhx.opencrd.receiver"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.rajbhx.opencrd.receiver"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = appVersionName
    }

    signingConfigs {
        if (signingEnabled) {
            create("release") {
                storeFile = file("release.keystore")
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            if (signingEnabled) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // SSH sign-in: pure-Java JSch fork (BSD-style JSch license), resolved from
    // Maven Central at build time. No native code — nothing is built with the NDK.
    implementation("com.github.mwiede:jsch:2.28.8")
}

// No AndroidX, no Google Play services, no NDK: the APK builds with the plain
// Android SDK, the Kotlin standard library and the pure-Java SSH library.
