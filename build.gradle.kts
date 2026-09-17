import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.4.20"
    id("generate-paper-libraries")
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.paperweight)
}

group = "com.github.encryptsl"

val pluginName = providers.gradleProperty("plugin_name").get()
val pluginVersion = providers.gradleProperty("plugin_version").get()
val pluginDescription = providers.gradleProperty("plugin_description").orNull ?: ""

version = pluginVersion
description = pluginDescription

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://central.sonatype.com/repository/maven-snapshots/") {
        name = "sonatype-snapshots"
        mavenContent {
            snapshotsOnly()
        }
    }
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
    maven("https://repo.rosewooddev.io/repository/public/")
    mavenLocal()
    flatDir {
        dirs("lib")
    }
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

dependencies {
    // Minecraft / Paper
    paperweight.paperDevBundle(providers.gradleProperty("server_version").get())

    compileOnly(libs.placeholderapi)
    compileOnly(libs.vaultunlocked)
    compileOnly(libs.miniplaceholders.api)
    compileOnly(libs.bundles.economy.plugins)

    implementation(libs.hikaricp)
    implementation(libs.bundles.exposed)
    implementation(libs.bundles.database.drivers)
    implementation(libs.bundles.flyway)
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.cloud)
    implementation(libs.configlib)
    implementation(libs.commons.csv)
    implementation(libs.config.updater)
    implementation(libs.ktor.client.core) {
        exclude("org.jetbrains.kotlinx", "kotlinx-io-core")
    }
    implementation(libs.bundles.ktor)

    implementation(libs.caffeine)

    // Internal implementations
    implementation(libs.bstats)
    implementation(libs.miniplaceholders)

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("io.mockk:mockk:1.14.11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.0")
    testImplementation(libs.vaultunlocked)

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
        java { srcDir("src/main/java") }
        kotlin { srcDir("src/main/kotlin") }
    }
}

tasks {
    processResources {
        dependsOn("generatePaperLibrariesYaml")

        val expandProps = mapOf(
            "name" to pluginName,
            "version" to pluginVersion,
            "description" to pluginDescription,
        )

        inputs.properties(expandProps)

        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(expandProps)
        }
    }

    shadowJar {
        archiveFileName.set("$pluginName-$pluginVersion.jar")

        duplicatesStrategy = DuplicatesStrategy.WARN

        exclude("META-INF/LICENSE*")
        exclude("META-INF/NOTICE*")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/DEPENDENCIES")

        relocate("org.bstats", "com.github.encryptsl.metrics")

        configurations = listOf(project.configurations.getByName("runtimeClasspath"))

        dependencies {
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
            exclude(dependency("org.jetbrains.kotlinx:.*:.*"))
            exclude(dependency("org.jetbrains.exposed:.*:.*"))
            exclude(dependency("io.ktor:.*:.*"))
            exclude(dependency("org.incendo:.*:.*"))
            exclude(dependency("com.zaxxer:.*:.*"))
            exclude(dependency("org.flywaydb:.*:.*"))
            exclude(dependency("org.mariadb.jdbc:.*:.*"))
            exclude(dependency("org.postgresql:.*:.*"))
            exclude(dependency("org.xerial:.*:.*"))
            exclude(dependency("de.exlll:.*:.*"))
            exclude(dependency("com.tchristofferson:.*:.*"))
            exclude(dependency("org.apache.commons:.*:.*"))
            exclude(dependency("com.github.ben-manes.caffeine:caffeine"))
        }

        mergeServiceFiles()
    }
    test {
        useJUnitPlatform()
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
        options.compilerArgs.add("-Xlint:deprecation")
    }

    build {
        dependsOn(shadowJar)
    }
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION