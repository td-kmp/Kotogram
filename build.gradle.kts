import org.jetbrains.kotlin.gradle.dsl.HasConfigurableKotlinCompilerOptions

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.multiplatform.library) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.nucleus) apply false
    alias(libs.plugins.nna) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

spotless {
    kotlin {
        target(sourceTree("**/src/**/*.kt"))
        ktlint(libs.versions.ktlint.get())
    }
    kotlinGradle {
        target(sourceTree("**/*.gradle.kts"))
        ktlint(libs.versions.ktlint.get())
    }
}

// Kotlin 2.4's experimental KEEP features; local type aliases and companion blocks would mark every binary pre-release.
subprojects {
    val kotlinPlugins = listOf(
        "org.jetbrains.kotlin.multiplatform",
        "org.jetbrains.kotlin.jvm",
        "com.android.application",
    )
    kotlinPlugins.forEach { id ->
        pluginManager.withPlugin(id) {
            val kotlin = extensions.getByName("kotlin") as HasConfigurableKotlinCompilerOptions<*>
            kotlin.compilerOptions.freeCompilerArgs.addAll(
                "-Xcollection-literals",
                "-Xcontext-sensitive-resolution",
                "-Xexplicit-context-arguments",
                "-Xname-based-destructuring=only-syntax",
                "-Xreturn-value-checker=check",
            )
        }
    }
}

fun sourceTree(pattern: String) = fileTree(projectDir) {
    include(pattern)
    exclude("**/build/**", "**/.gradle/**", "**/.kotlin/**")
}

dependencies {
    detektPlugins(libs.detekt.rules.compose)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(file("config/detekt/detekt.yml"))
    basePath.set(projectDir)
    baseline = file("config/detekt/baseline.xml")
    parallel = true
    source.setFrom(
        fileTree(projectDir) {
            include("**/src/**/*.kt")
            exclude("**/build/**")
        },
    )
}
