import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "generatePaperLibrariesYaml.gradle.kts")

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "9.0.0-beta17"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
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
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    paperweight.paperDevBundle(providers.gradleProperty("server_version").get())
    compileOnly(libs.vaultapi) {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly(libs.placeholderapi)

    compileOnly(libs.exposed.core)
    compileOnly(libs.exposed.jdbc)
    compileOnly(libs.exposed.kotlin.datetime)

    compileOnly(libs.hikaricp)
    compileOnly(libs.flyway.core)

    compileOnly(libs.vaultunlocked)

    compileOnly(libs.okhttp)
    compileOnly(libs.config.updater)
    compileOnly(libs.commons.csv)

    compileOnly(libs.cloud.annotations) {
        exclude("org.incendo", "cloud-core")
    }
    compileOnly(libs.cloud.paper)
    compileOnly(libs.cloud.extras) {
        exclude("org.incendo", "cloud-core")
        exclude("net.kyori", "adventure-text-api")
        exclude("net.kyori", "adventure-text-minimessage")
        exclude("net.kyori", "adventure-text-serializer-plain")
    }
    compileOnly(libs.cloud.kotlin)

    compileOnly(libs.bettereconomy)
    compileOnly(libs.scruffyeconomy)
    compileOnly(libs.craftconomy)

    runtimeOnly(libs.mariadb)
    runtimeOnly(libs.flyway.mysql)
    runtimeOnly(libs.postqresql)
    runtimeOnly(libs.kotlin.stdlib.jdk8)

    implementation(libs.bstats)
    implementation(libs.miniplaceholders)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)

    testImplementation(libs.mariadb)
    testImplementation(libs.flyway.core.test)
    testImplementation(libs.hikaricp)
    testImplementation(libs.sqlite)
    testImplementation(libs.exposed.core)
    testImplementation(libs.exposed.jdbc)
    testImplementation(libs.exposed.migration)
    testImplementation(libs.exposed.kotlin.datetime)
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
        }
        exclude("kotlin/**")
        exclude("kotlinx/**")
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
