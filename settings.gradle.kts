rootProject.name = "Frac2lz_compose"
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
        maven(url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev/"))
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
        mavenCentral()
        maven(url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev/"))
//        maven(url = uri("https://mvnrepository.com/artifact/org.jetbrains.skiko/skiko-awt-runtime-windows-x64"))
    }
}

include(":composeApp")