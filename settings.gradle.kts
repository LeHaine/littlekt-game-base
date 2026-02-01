pluginManagement {
    repositories {
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "littlekt-game-base"
include("game")
include("littlekt-extras:core")