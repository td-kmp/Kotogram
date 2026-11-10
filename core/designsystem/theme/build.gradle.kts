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
        namespace = "com.xbot.kotogram.core.designsystem.theme"
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

    compilerOptions {
        optIn.add("com.xbot.kotogram.core.designsystem.theme.InternalDesignSystemApi")
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(libs.compose.runtime)
        api(libs.compose.foundation)
        api(libs.compose.ui)
        api(libs.compose.material3)
        implementation(projects.core.resources)
        implementation(libs.compose.preview)
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.window)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        jvmMain.dependencies {
            api(libs.nucleus.application)
            implementation(libs.nucleus.decorated.window.material3)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
