import com.littlekt.gradle.texturepacker.littleKt
import com.littlekt.gradle.texturepacker.packing
import com.littlekt.gradle.texturepacker.texturePacker
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinJsCompilerType
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

repositories {
    maven(url = "https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.littlekt.gradle.texturepacker)
}

littleKt {
    texturePacker {
        inputDir = "../art/export_tiles/"
        outputDir = "src/commonMain/resources/"
        outputName = "tiles.atlas"
        packing {
            extrude = 2
        }
    }
}

group = "com.lehaine.game"
version = "1.0"

kotlin {
    tasks.withType<JavaExec> { jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED") }
    applyDefaultHierarchyTemplate()
    jvm {
        compilations {
            val main by getting

            val mainClass = (findProperty("jvm.mainClass") as? String)?.plus("Kt")
                ?: project.logger.log(
                    LogLevel.ERROR,
                    "Property 'jvm.mainClass' has either changed or has not been set. Check 'gradle.properties' and ensure it is properly set!"
                )
            tasks {
                register<Copy>("copyResources") {
                    group = "package"
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    from(main.output.resourcesDir)
                    destinationDir = File("$buildDir/publish")
                }
                register<Jar>("packageFatJar") {
                    group = "package"
                    archiveClassifier.set("all")
                    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                    dependsOn(named("jvmJar"))
                    dependsOn(named("copyResources"))
                    manifest {
                        attributes["Main-Class"] = mainClass
                    }
                    destinationDirectory.set(File("$buildDir/publish/"))
                    from(
                        main.runtimeDependencyFiles.map { if (it.isDirectory) it else zipTree(it) },
                        main.output.classesDirs
                    )
                    doLast {
                        project.logger.lifecycle("[LittleKt] The packaged jar is available at: ${outputs.files.first().parent}")
                    }
                }
            }
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(KotlinJsCompilerType.IR) {
        binaries.executable()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }

        this.attributes.attribute(
            KotlinPlatformType.attribute,
            KotlinPlatformType.js
        )

        compilations.all {
            kotlinOptions.sourceMap = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.littlekt.core)
                implementation(libs.littlekt.scenegraph)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.fleks)
                implementation(project(":littlekt-extras:core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(libs.kotlinx.html.js)
            }
        }
        val jsTest by getting
    }
}

rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
    versions.webpackCli.version = "4.10.0"
}
