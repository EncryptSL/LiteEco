import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.papermc.paperweight.userdev") version "1.3.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    kotlin("jvm") version "1.6.21" apply true
}

group = "encryptsl.cekuj.net"
version = providers.gradleProperty("plugin_version").get()
description = providers.gradleProperty("plugin_description").get()

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    flatDir {
        dirs("lib")
    }
}

dependencies {
    paperDevBundle(providers.gradleProperty("server_version").get())
    compileOnly(kotlin("stdlib", "1.6.21"))
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("com.zaxxer:HikariCP:5.0.1")
    compileOnly("me.clip:placeholderapi:2.11.1")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.0")
    testImplementation(kotlin("test"))
}

tasks {

    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    reobfJar {
        outputJar.set(layout.buildDirectory.file("libs/${providers.gradleProperty("plugin_name").get()}-${version}.jar"))
    }

    shadowJar {
        minimize {
            relocate("co.aikar.commands", "encryptsl.cekuj.net.co.aikar.commands")
            relocate("co.aikar.locale", "encryptsl.cekuj.net.co.aikar.locale")
            relocate("org.bstats", "encryptsl.cekuj.net.api.bstats")
        }
    }
}