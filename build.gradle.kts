import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "generatePaperLibrariesYaml.gradle.kts")

plugins {
    kotlin("jvm") version "2.3.0"
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.paperweight)
}

group = "com.github.encryptsl"
version = providers.gradleProperty("plugin_version").get()
description = providers.gradleProperty("plugin_description").get()

val props = project.properties.mapValues { it.value.toString() }

repositories {
    flatDir {
        dirs("lib")
    }
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
    maven("https://repo.rosewooddev.io/repository/public/")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    // Minecraft / Paper
    paperweight.paperDevBundle(providers.gradleProperty("server_version").get())

    // API & plugins
    compileOnly(libs.placeholderapi)
    compileOnly(libs.vaultunlocked)
    compileOnly(libs.miniplaceholders.api)

    // Databases & migrations
    compileOnly(libs.hikaricp)
    compileOnly(libs.bundles.exposed)
    compileOnly(libs.bundles.database.drivers)

    // ⚠️ Flyway
    compileOnly(libs.bundles.flyway)

    // Kotlin
    compileOnly(libs.bundles.kotlin)

    // Cloud Command Framework
    compileOnly(libs.bundles.cloud) {
        exclude("net.kyori", "adventure-text-api")
        exclude("net.kyori", "adventure-text-minimessage")
        exclude("net.kyori", "adventure-text-serializer-plain")
    }

    // Misc
    compileOnly(libs.commons.csv)
    compileOnly(libs.config.updater)
    compileOnly(libs.ktor.client.core) {
        exclude("org.jetbrains.kotlinx", "kotlinx-io-core")
    }

    // Economy plugins
    compileOnly(libs.bundles.economy.plugins)

    // Implementations
    implementation(libs.bstats)
    implementation(libs.miniplaceholders)
    implementation(libs.bundles.ktor)


    // Test
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bundles.exposed)
    testImplementation(libs.hikaricp)
    testImplementation(libs.bundles.database.drivers)

    testRuntimeOnly(libs.junit.launcher)
    testImplementation(libs.flyway.core.test)
}

sourceSets {
    getByName("main") {
        java {
            srcDir("src/main/java")
        }
        kotlin {
            srcDir("src/main/kotlin")
        }
    }
}

tasks {
    processResources {
        dependsOn("generatePaperLibrariesYaml")
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(props)
        }
    }

    shadowJar {
        archiveFileName.set("${providers.gradleProperty("plugin_name").get()}-${project.version}.jar")
        minimize {
            relocate("org.bstats", "com.github.encryptsl.metrics")
            relocate("io.ktor", "com.github.encryptsl.ktor")
        }

        mergeServiceFiles()
        // Needed for mergeServiceFiles to work properly in Shadow 9+
        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }

        exclude("kotlin/**")
        exclude("sqlite/**")
    }
    test {
        useJUnitPlatform()
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
        options.compilerArgs.add("-Xlint:deprecation")
    }
    build {
        dependsOn(shadowJar)
    }
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
