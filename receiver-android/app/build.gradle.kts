plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Signing is enabled only when CI materializes the keystore and exports
// ANDROID_KEYSTORE_BASE64 (see .github/workflows/release.yml). Local builds
// and debug builds never touch the release key.
val signingEnabled = System.getenv("ANDROID_KEYSTORE_BASE64") != null
val appVersionName = System.getenv("OPENCRD_VERSION") ?: "0.1.0"

android {
    namespace = "io.github.rajbhx.opencrd.receiver"
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.rajbhx.opencrd.receiver"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
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

// Intentionally zero library dependencies: no AndroidX, no Google Play
// services, no native code (no NDK). The APK builds with the plain Android
// SDK + Kotlin standard library only.
