import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.xbot.kotogram.feature.auth.domain"
        compileSdk {
            version = release(libs.versions.android.compilesdk.get().toInt())
        }
        minSdk {
            version = release(libs.versions.android.minsdk.get().toInt())
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    jvmToolchain(21)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(projects.core.domain)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.koin.core)
        implementation(libs.koin.annotations)
    }
}

koinCompiler {
    compileSafety = true
}
