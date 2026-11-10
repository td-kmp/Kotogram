import dev.nucleusframework.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.nucleus)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core.designsystem.theme)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.nucleus.application)
    implementation(libs.nucleus.decorated.window.tao)
    implementation(libs.nucleus.decorated.window.material3)
}

nucleus.application {
    mainClass = "MainKt"
    // FFM downcalls into core:designsystem:appkit's Kotlin/Native library.
    jvmArgs += listOf("--enable-native-access=ALL-UNNAMED")

    nativeDistributions {
        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "Kotogram"
        packageVersion = "1.0.0"

        macOS {
            bundleID = "com.xbot.kotogram"
            dockName = "Kotogram"
        }
    }
}
