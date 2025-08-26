val generatePaperLibrariesYaml by tasks.registering {
    group = "build setup"
    description = "Generates paper-libraries.yml from all declared dependencies"

    val outputFile = file("src/main/resources/paper-libraries.yml")

    val allowedDependencies = setOf(
        "com.zaxxer:HikariCP",
        "org.mariadb.jdbc:mariadb-java-client",
        "org.postgresql:postgresql",
        "org.xerial:sqlite-jdbc",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
        "org.jetbrains.kotlin:kotlin-reflect",
        "org.jetbrains.kotlinx:kotlinx-io-core",
        "org.jetbrains.exposed:exposed-core",
        "org.jetbrains.exposed:exposed-jdbc",
        "org.jetbrains.exposed:exposed-kotlin-datetime",
        "org.flywaydb:flyway-core",
        "org.flywaydb:flyway-mysql",
        "io.ktor:ktor-client-core",
        "com.tchristofferson:ConfigUpdater",
        "org.apache.commons:commons-csv",
        "org.incendo:cloud-annotations",
        "org.incendo:cloud-minecraft-extras",
        "org.incendo:cloud-kotlin-extensions",
        "org.incendo:cloud-paper"
    )

    doLast {
        val includedScopes = listOf("compileOnly", "implementation", "api", "runtimeOnly")

        val dependencies = configurations
            .matching { it.name in includedScopes }
            .flatMap { config ->
                config.dependencies
            }
            .filterIsInstance<ModuleDependency>()
            .mapNotNull {
                val group = it.group ?: return@mapNotNull null
                val name = it.name
                val version = it.version ?: return@mapNotNull null
                val ga = "$group:$name"
                if (ga in allowedDependencies) "$group:$name:$version" else null
            }
            .distinct()
            .sorted()

        val content = buildString {
            appendLine("libraries:")
            dependencies.forEach {
                appendLine("  - $it")
            }
        }

        outputFile.parentFile.mkdirs()
        outputFile.writeText(content)
        println("✅ paper-libraries.yml generated with ${dependencies.size} libraries")
    }
}