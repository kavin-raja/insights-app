pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
    plugins {
        kotlin("jvm") version "1.9.10"  // your Kotlin version
        id("org.jetbrains.kotlin.android") version "1.9.10"
        id("org.jetbrains.kotlin.plugin.compose") version "1.9.10" // Compose compiler plugin
    }

}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "InsightsApp"
include(":app")
