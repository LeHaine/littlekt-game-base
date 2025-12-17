plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    //alias(libs.plugins.littlekt.gradle.texturepacker) apply false
}

allprojects {
    repositories {
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
        google()
        mavenCentral()
        mavenLocal()
    }
}

plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>().apply {
        yarnLockMismatchReport = org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport.WARNING
    }
}
