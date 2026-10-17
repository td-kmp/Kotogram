rootProject.name = "Kotogram"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        // tdlib-kmp releases not yet on Maven Central: `./gradlew publishToMavenLocal` in tdlib-kmp.
        mavenLocal {
            mavenContent {
                includeGroup("io.github.xephosbot")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":android-app")
include(":jvm-app")
include(":shared")
include(":core:designsystem")
include(":core:domain")
include(":core:navigation:api")
include(":core:navigation:impl")
include(":core:tdlib")
include(":feature:auth:api")
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:presentation")
include(":feature:chats:api")
include(":feature:chats:presentation")
