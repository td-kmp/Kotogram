import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.xbot.kotogram.feature.chats.presentation"
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

    jvmToolchain(21)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(projects.feature.chats.domain)
        implementation(projects.feature.chats.api)
        implementation(projects.core.navigation.api)
        implementation(projects.core.designsystem)
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
        implementation(libs.compose.material3)
        implementation(libs.compose.resources)
        implementation(libs.compose.preview)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.orbit.core)
        implementation(libs.orbit.viewmodel)
        implementation(libs.orbit.compose)
        implementation(libs.koin.core)
        implementation(libs.koin.core.viewmodel)
        implementation(libs.koin.compose)
        implementation(libs.koin.compose.viewmodel)
        implementation(libs.koin.compose.navigation3)
        implementation(libs.koin.annotations)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
    }
}

koinCompiler {
    compileSafety = true
}

compose.resources {
    packageOfResClass = "com.xbot.kotogram.feature.chats.presentation.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
