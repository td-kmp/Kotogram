import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.koin.compiler)
}

kotlin {
    android {
        namespace = "com.xbot.kotogram.core.tdlib"
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

    jvmToolchain(21)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    dependencies {
        api(projects.core.domain)
        api(libs.tdlib.client)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.io.core)
        implementation(libs.androidx.datastore.preferences.core)
        implementation(libs.okio)
        implementation(libs.kermit)
        implementation(libs.koin.core)
        implementation(libs.koin.annotations)
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

koinCompiler {
    compileSafety = true
}

buildkonfig {
    packageName = "com.xbot.kotogram.core.tdlib"
    objectName = "TelegramApiConfig"

    defaultConfigs {
        buildConfigField(
            INT,
            "API_ID",
            localProperties.getProperty("telegram.api.id") ?: System.getenv("TELEGRAM_API_ID")
                ?: "0"
        )
        buildConfigField(
            STRING,
            "API_HASH",
            localProperties.getProperty("telegram.api.hash") ?: System.getenv("TELEGRAM_API_HASH")
                ?: ""
        )
        // TDLib's log verbosity (0 fatal … 5 verbose), logged through Kermit: -Ptdlib.log.verbosity=N, or
        // tdlib.log.verbosity=N in local.properties.
        buildConfigField(
            INT,
            "TDLIB_LOG_VERBOSITY",
            providers.gradleProperty("tdlib.log.verbosity").orNull
                ?: localProperties.getProperty("tdlib.log.verbosity")
                ?: "4"
        )
        // Telegram's test servers: -Ptelegram.test.dc=true, or telegram.test.dc=true in local.properties.
        buildConfigField(
            BOOLEAN,
            "USE_TEST_DC",
            providers.gradleProperty("telegram.test.dc").orNull
                ?: localProperties.getProperty("telegram.test.dc")
                ?: "false"
        )
    }
}
