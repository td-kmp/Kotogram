plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.nna)
}

kotlin {
    jvm()
    macosArm64()

    jvmToolchain(25)

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        named("macosArm64Main") {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlinx.cinterop.BetaInteropApi")
            }
        }
    }
}

kotlinNativeExport {
    nativeLibName = "MacAppKit"
    // Workaround: without it the proxies' package is that of the first source file by path.
    nativePackage = "com.xbot.kotogram.core.designsystem.appkit"
}
