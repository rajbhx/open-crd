// Root build script. Versions are pinned for reproducibility in CI.
// Gradle 8.10.2 / JDK 17 / AGP 8.7.3 / Kotlin 2.0.21 (see .github/workflows/android.yml).
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}
