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
        // tdlib-kmp 1.8.69-1.0.0-alpha03 is a local build until it is published (docs/handoff.md).
        mavenLocal {
            mavenContent {
                includeGroup("io.github.xephosbot")
            }
        }
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
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
include(":catalog-app")
include(":web-app")
include(":shared")
include(":core:account")
include(":core:designsystem")
include(":core:resources")
include(":core:navigation:api")
include(":core:navigation:impl")
include(":feature:auth:api")
include(":feature:auth:domain")
include(":feature:auth:data")
include(":feature:auth:impl")
include(":feature:chats:api")
include(":feature:chats:domain")
include(":feature:chats:data")
include(":feature:chats:impl")
include(":feature:language:domain")
include(":feature:language:data")
include(":feature:language:impl")
