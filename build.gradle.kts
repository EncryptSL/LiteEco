import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0" apply true
    id("io.github.goooler.shadow") version "8.1.7"
    id("maven-publish")
}

group = "com.github.encryptsl"
version = providers.gradleProperty("plugin_version").get()
description = providers.gradleProperty("plugin_description").get()

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
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${providers.gradleProperty("server_version").get()}")
    compileOnly(kotlin("stdlib", "2.0.0"))
    compileOnly("me.lokka30:treasury-api:2.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("org.jetbrains.exposed:exposed-core:0.50.1")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    compileOnly("org.jetbrains.exposed:exposed-kotlin-datetime:0.50.1")
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
    compileOnly("me.hsgamer:bettereconomy:3.1")

    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("org.incendo:cloud-paper:2.0.0-SNAPSHOT")
    implementation("org.incendo:cloud-annotations:2.0.0-SNAPSHOT") {
        exclude("org.incendo", "cloud-core")
    }
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-SNAPSHOT") {
        exclude("org.incendo", "cloud-core")
        exclude("net.kyori", "adventure-text-api")
        exclude("net.kyori", "adventure-text-minimessage")
        exclude("net.kyori", "adventure-text-serializer-plain")
    }
    implementation("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.2.3")

    testImplementation(kotlin("test", "2.0.0"))
    testImplementation("com.zaxxer:HikariCP:5.1.0")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
    testImplementation("org.jetbrains.exposed:exposed-core:0.50.1")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
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

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(project.properties)
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
        options.compilerArgs.add("-Xlint:deprecation")
    }
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
    shadowJar {
        archiveFileName.set("${providers.gradleProperty("plugin_name").get()}-$version.jar")
        minimize {
            relocate("org.bstats", "com.github.encryptsl.metrics")
            relocate("org.incendo", "com.github.encryptsl.cloud-core")
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
    publications.create<MavenPublication>("libs").from(components["kotlin"])
}