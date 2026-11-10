plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.nucleus)
    alias(libs.plugins.roborazzi)
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation(projects.core.designsystem.components)
    implementation(projects.core.designsystem.icons)
    implementation(compose.desktop.currentOs)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.nucleus.application)
    implementation(libs.nucleus.decorated.window.tao)
    implementation(libs.nucleus.decorated.window.material3)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.compose.ui.test)
    testImplementation(libs.roborazzi.compose.desktop)
}

nucleus.application {
    mainClass = "MainKt"
    // FFM downcalls into core:designsystem:appkit's Kotlin/Native library.
    jvmArgs += listOf("--enable-native-access=ALL-UNNAMED")
}
