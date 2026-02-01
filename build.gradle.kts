import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.littlekt.gradle.texturepacker) apply false
}

allprojects {
    repositories {
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/")
        google()
        mavenCentral()
        mavenLocal()
    }
}

plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().apply { yarnLockMismatchReport = YarnLockMismatchReport.WARNING }
}