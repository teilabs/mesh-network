pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.library") version "8.2.0" apply false
        id("org.jetbrains.kotlin.android") version "1.9.22" apply false  // ← Единая версия
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "mesh-network"
include(":core")
include(":client-android")
project(":client-android").projectDir = file("client/android")