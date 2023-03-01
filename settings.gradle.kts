pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "littlekt-game-base"
include("game")
include("littlekt-extras:core")

enableFeaturePreview("VERSION_CATALOGS")