plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.nna)
}

kotlin {
    jvm()
    linuxX64 {
        compilations.getByName("main").cinterops.create("netlink")
    }

    jvmToolchain(25)

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        named("linuxX64Main") {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
            }
        }
    }
}

kotlinNativeExport {
    nativeLibName = "LinuxPlatform"
    // Workaround: without it the proxies' package is that of the first source file by path.
    nativePackage = "com.xbot.kotogram.core.platform.linux"
}
