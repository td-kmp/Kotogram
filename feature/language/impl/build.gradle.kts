import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.xbot.kotogram.feature.language.impl"
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

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    jvmToolchain(25)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        implementation(projects.feature.language.domain)
        implementation(projects.core.account)
        implementation(projects.core.resources)
        implementation(libs.compose.runtime)
        implementation(libs.koin.core)
        implementation(libs.kotlinx.coroutines.core)
    }

    sourceSets {
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
