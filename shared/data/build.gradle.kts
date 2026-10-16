import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.INT
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import java.util.Properties
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.xbot.kotogram.shared.data"
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
        api(projects.shared.domain)
        api(libs.tdlib.client)
        api(libs.koin.core)
        implementation(libs.kotlinx.coroutines.core)
    }
}

// Telegram API credentials come from local.properties (telegram.api.id / telegram.api.hash) or the
// TELEGRAM_API_ID / TELEGRAM_API_HASH environment variables, so they never land in git.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

buildkonfig {
    packageName = "com.xbot.kotogram.data.tdlib"
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
    }
}
