import io.papermc.paperweight.userdev.ReobfArtifactConfiguration
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

apply(from = "generatePaperLibrariesYaml.gradle.kts")

plugins {
    kotlin("jvm") version "2.3.20"
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.paperweight)
}

group = "com.github.encryptsl"
version = providers.gradleProperty("plugin_version").get()
description = providers.gradleProperty("plugin_description").get()

val props = project.properties.mapValues { it.value.toString() }

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven {
        url = uri("https://central.sonatype.com/repository/maven-snapshots/")
        content {
            includeGroup("org.incendo")
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
        freeCompilerArgs.add("-Xcontext-parameters")
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

    // Internal implementations (You can even shadow these if you want)
    implementation(libs.bstats)
    implementation(libs.miniplaceholders)

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
        // Triggers generation before the resources process
        dependsOn("generatePaperLibrariesYaml")
        filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
            expand(props)
        }
    }

    shadowJar {
        archiveFileName.set("${providers.gradleProperty("plugin_name").get()}-${project.version}.jar")

        // We will keep the relocations
        relocate("org.bstats", "com.github.encryptsl.metrics")

        // This prevents ShadowJAR from packaging third-party libraries,
        // generator will still be able to see them in the runtimeClasspath.
        configurations = emptyList()

        // bStats and MiniPlaceholders to be included in the JAR (since they aren't in the paper-libraries)        from(sourceSets.main.get().output)
        configurations = listOf(project.configurations.getByName("runtimeClasspath"))

        // We'll exclude the groups listed in paper-libraries.yml file
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