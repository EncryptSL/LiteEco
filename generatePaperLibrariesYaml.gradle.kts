tasks.register("generatePaperLibrariesYaml") {
    group = "build setup"
    description = "Generates paper-libraries.yml from all declared dependencies"

    val outputFile = project.layout.projectDirectory.file("src/main/resources/paper-libraries.yml").asFile
    val compileClasspath = project.configurations.named("compileClasspath")

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

    inputs.files(compileClasspath).withPropertyName("compileClasspath")
    outputs.file(outputFile).withPropertyName("outputFile")

    doLast {
        val configuration = compileClasspath.get()

        val dependencies = configuration.resolvedConfiguration.lenientConfiguration.allModuleDependencies
            .map { it.module.id }
            .filter { id -> "${id.group}:${id.name}" in allowedDependencies }
            .map { id -> "${id.group}:${id.name}:${id.version}" }
            .distinct()
            .sorted()

        val content = buildString {
            appendLine("libraries:")
            dependencies.forEach {
                appendLine("  - $it")
            }
        }

        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        outputFile.writeText(content)
        println("✅ paper-libraries.yml generated with ${dependencies.size} libraries")
    }
}