rootProject.name = "Kotogram"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        // Nucleus's decorated-window-tao is a local build of the fork's placedAsOverlay over the release (docs/handoff.md).
        mavenLocal {
            mavenContent {
                includeGroupAndSubgroups("dev.nucleusframework")
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
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        // Nucleus's decorated-window-tao is a local build of the fork's placedAsOverlay over the release (docs/handoff.md).
        mavenLocal {
            mavenContent {
                includeGroupAndSubgroups("dev.nucleusframework")
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
include(":core:designsystem:theme")
include(":core:designsystem:components")
include(":core:designsystem:icons")
include(":core:designsystem:appkit")
include(":core:platform:linux")
include(":core:platform:macos")
include(":core:platform:windows")
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
include(":feature:connection:domain")
include(":feature:connection:data")
include(":feature:connection:impl")
include(":feature:language:domain")
include(":feature:language:data")
include(":feature:language:impl")
include(":feature:media:domain")
include(":feature:media:data")
