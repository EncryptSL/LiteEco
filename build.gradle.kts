import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.0-RC"
    id("com.gradleup.shadow") version "9.0.0-beta15"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17"
    id("maven-publish")
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
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("me.clip:placeholderapi:2.11.5")

    //Exposed Database Orm
    compileOnly("org.jetbrains.exposed:exposed-core:0.60.0")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:0.60.0")
    compileOnly("org.jetbrains.exposed:exposed-kotlin-datetime:0.60.0")
    //Database Pool HikariCP
    compileOnly("com.zaxxer:HikariCP:6.2.1")

    //EconomyAPI
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.11")

    //Coding Utils
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")
    compileOnly("com.tchristofferson:ConfigUpdater:2.2-SNAPSHOT")
    compileOnly("org.apache.commons:commons-csv:1.14.0")

    //Command Framework
    compileOnly("org.incendo:cloud-annotations:2.0.0-SNAPSHOT") {
        exclude("org.incendo", "cloud-core")
    }
    compileOnly("org.incendo:cloud-paper:2.0.0-SNAPSHOT")
    compileOnly("org.incendo:cloud-minecraft-extras:2.0.0-SNAPSHOT") {
        exclude("org.incendo", "cloud-core")
        exclude("net.kyori", "adventure-text-api")
        exclude("net.kyori", "adventure-text-minimessage")
        exclude("net.kyori", "adventure-text-serializer-plain")
    }
    compileOnly("org.incendo:cloud-kotlin-extensions:2.0.0")

    // Plugins for import economy to LiteEco
    compileOnly("me.hsgamer:bettereconomy:3.1")
    compileOnly("me.scruffyboy13:Economy:2.0")
    compileOnly("com.greatmancode:Craftconomy3:3.3.3-SNAPSHOT")

    //Metrics
    implementation("org.bstats:bstats-bukkit:3.0.1")

    //MiniPlaceholders
    implementation("io.github.miniplaceholders:miniplaceholders-kotlin-ext:2.3.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mariadb.jdbc:mariadb-java-client:3.5.2")
    testImplementation("org.flywaydb:flyway-core:11.9.1")
    testImplementation("com.zaxxer:HikariCP:6.2.1")
    testImplementation("org.xerial:sqlite-jdbc:3.49.1.0")
    testImplementation("org.jetbrains.exposed:exposed-core:0.60.0")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.60.0")
    testImplementation("org.jetbrains.exposed:exposed-migration:0.60.0")
    testImplementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.60.0")
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

publishing {
    repositories {
        mavenLocal()
    }
    publications.create<MavenPublication>("libs").from(components["kotlin"])
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION
