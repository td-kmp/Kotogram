import dev.nucleusframework.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.nucleus)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.shared)
    implementation(projects.core.designsystem)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.nucleus.application)
    implementation(libs.nucleus.decorated.window.tao)
    implementation(libs.nucleus.decorated.window.material3)
}

nucleus.application {
    mainClass = "MainKt"

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
