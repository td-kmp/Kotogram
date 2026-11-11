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
        namespace = "com.xbot.kotogram.core.designsystem.components"
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
        api(projects.core.designsystem.theme)
        api(projects.core.resources)
        api(libs.coil.compose.core)
        implementation(projects.core.designsystem.icons)
        implementation(libs.okio)
        implementation(libs.androidx.lifecycle.runtime.compose)
        implementation(libs.compose.preview)
        implementation(libs.compottie)
        implementation(libs.compottie.dot)
        implementation(libs.haze)
        implementation(libs.haze.blur)
        implementation(libs.qrose)
        implementation(libs.qrose.encoder.matrix)
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        jvmMain.dependencies {
            implementation(projects.core.designsystem.appkit)
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
