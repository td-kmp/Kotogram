import com.android.build.api.withAndroid
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
}

// The macOS language pack keys our snake_case names stand for: Login.QR.Title for login_qr_title.
val generateLanguagePackKeys = tasks.register("generateLanguagePackKeys") {
    val keys = layout.projectDirectory.file("langpack/macos-keys.txt")
    val strings = layout.projectDirectory.file("src/commonMain/composeResources/values/strings.xml")
    val output = layout.buildDirectory.dir("generated/langpack")
    inputs.file(keys)
    inputs.file(strings)
    outputs.dir(output)
    doLast {
        val index = keys.asFile.readLines()
            .filter { it.isNotBlank() }
            .associateBy { it.lowercase().replace('.', '_') }
        val entries = Regex("""<(?:string|plurals) name="([^"]+)"""")
            .findAll(strings.asFile.readText())
            .mapNotNull { match ->
                match.groupValues[1].let { name -> index[name]?.let { name to it } }
            }
            .sortedBy { it.first }
            .toList()
        val file = output.get().file("com/xbot/kotogram/core/resources/LanguagePackKeys.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            buildString {
                appendLine("package com.xbot.kotogram.core.resources")
                appendLine()
                appendLine("// Generated from langpack/macos-keys.txt and values/strings.xml.")
                appendLine("internal val LanguagePackKeys: Map<String, String> = mapOf(")
                entries.forEach { (name, key) -> appendLine("    \"$name\" to \"$key\",") }
                appendLine(")")
            }
        )
    }
}

kotlin {
    android {
        namespace = "com.xbot.kotogram.core.resources"
        compileSdk {
            version = release(libs.versions.android.compilesdk.get().toInt())
        }
        minSdk {
            version = release(libs.versions.android.minsdk.get().toInt())
        }
        androidResources {
            enable = true
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

    // Language pack strings are read in the first frame where TDLib runs in the process, as Compose reads its own.
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    applyHierarchyTemplate {
        common {
            group("nonWeb") {
                withJvm()
                withAndroid()
                group("ios") {
                    withIosArm64()
                    withIosSimulatorArm64()
                }
            }
            group("web") {
                withWasmJs()
            }
        }
    }

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(libs.compose.resources)
        implementation(libs.compose.runtime)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.tdlib.client)
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateLanguagePackKeys)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.xbot.kotogram.core.resources"
}
