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
        namespace = "com.xbot.kotogram.core.designsystem"
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
        api(libs.compose.runtime)
        api(libs.compose.foundation)
        api(libs.compose.ui)
        api(libs.compose.material3)
        api(projects.core.resources)
        implementation(libs.compose.preview)
        implementation(libs.compottie)
        implementation(libs.compottie.dot)
        implementation(libs.qrose)
        implementation(libs.qrose.encoder.matrix)
    }

    sourceSets {
        iosMain.dependencies {
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        jvmMain.dependencies {
            implementation(libs.jna)
            implementation(libs.nucleus.decorated.window.tao)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.compose.ui.test)
            implementation(compose.desktop.currentOs)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}
