import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    android {
        namespace = "com.xbot.kotogram.feature.auth.presentation"
        compileSdk {
            version = release(libs.versions.android.compilesdk.get().toInt())
        }
        minSdk {
            version = release(libs.versions.android.minsdk.get().toInt())
        }
        androidResources {
            enable = true
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    jvmToolchain(21)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(projects.feature.auth.domain)
        implementation(libs.compose.runtime)
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
        implementation(libs.compose.material3)
        implementation(libs.compose.resources)
        implementation(libs.compose.preview)
        implementation(libs.calf.ui)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.androidx.lifecycle.viewmodel.navigation3)
        implementation(libs.androidx.navigation3.ui)
        implementation(libs.orbit.core)
        implementation(libs.orbit.viewmodel)
        implementation(libs.orbit.compose)
        implementation(libs.koin.compose.viewmodel)
        implementation(libs.kotlinx.coroutines.core)
    }
}

compose.resources {
    packageOfResClass = "com.xbot.kotogram.feature.auth.presentation.resources"
}
