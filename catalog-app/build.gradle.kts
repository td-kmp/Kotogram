plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.nucleus)
    alias(libs.plugins.roborazzi)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(projects.core.designsystem)
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
}
