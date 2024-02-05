plugins {
    kotlin("jvm") version "1.9.22" apply true
    id("io.papermc.paperweight.userdev") version "1.5.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("maven-publish")
}

group = "encryptsl.cekuj.net"
version = providers.gradleProperty("plugin_version").get()
description = providers.gradleProperty("plugin_description").get()

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    paperweight.paperDevBundle(providers.gradleProperty("server_version").get())
    compileOnly(kotlin("stdlib", "1.9.22"))
    compileOnly("me.lokka30:treasury-api:1.2.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("org.jetbrains.exposed:exposed-core:0.46.0")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    compileOnly("com.squareup.okhttp3:okhttp:4.12.0")

    implementation("org.bstats:bstats-bukkit:3.0.1")
    implementation("org.incendo:cloud-paper:2.0.0-beta.2")
    implementation("org.incendo:cloud-annotations:2.0.0-beta.2")

    testImplementation(kotlin("test", "1.9.22"))
    testImplementation("com.zaxxer:HikariCP:5.1.0")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
    testImplementation("org.jetbrains.exposed:exposed-core:0.46.0")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
}

tasks {

    build {
        dependsOn(reobfJar)
    }

    test {
        useJUnitPlatform()
    }

    processResources {
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }

    reobfJar {
        outputJar.set(layout.buildDirectory.file("libs/${providers.gradleProperty("plugin_name").get()}-${version}.jar"))
    }

    shadowJar {
        minimize {
            relocate("org.bstats", "com.github.encryptsl.metrics")
            relocate("cloud.commandframework", "com.github.encryptsl.cloud-core")
        }
    }
}

publishing {
    repositories {
        mavenLocal()
    }
    publications.create<MavenPublication>("libs").from(components["kotlin"])
}