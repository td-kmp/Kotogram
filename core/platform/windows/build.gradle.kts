plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.nna)
}

kotlin {
    jvm()
    mingwX64 {
        compilations.getByName("main").cinterops.create("netlistmgr")
    }

    jvmToolchain(25)

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        named("mingwX64Main") {
            languageSettings {
                optIn("kotlinx.cinterop.ExperimentalForeignApi")
                optIn("kotlinx.coroutines.DelicateCoroutinesApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlin.concurrent.atomics.ExperimentalAtomicApi")
            }
        }
    }
}

kotlinNativeExport {
    nativeLibName = "WindowsPlatform"
    // Workaround: without it the proxies' package is that of the first source file by path.
    nativePackage = "com.xbot.kotogram.core.platform.windows"
}
