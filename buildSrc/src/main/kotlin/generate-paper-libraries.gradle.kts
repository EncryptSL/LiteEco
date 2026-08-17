abstract class GeneratePaperLibrariesTask : DefaultTask() {

    @get:InputFiles
    abstract val classpathConfiguration: Property<Configuration>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @get:Input
    abstract val allowedGroups: SetProperty<String>

    @TaskAction
    fun generate() {
        val groups = allowedGroups.get()
        val config = classpathConfiguration.get()

        // Resolvujeme závislosti přímo z předané konfigurace bez použití 'project'
        val dependencies = config.resolvedConfiguration.resolvedArtifacts
            .map { it.moduleVersion.id }
            .filter { id -> groups.any { group -> id.group.startsWith(group) } }
            .map { id -> "${id.group}:${id.name}:${id.version}" }
            .distinct()
            .sorted()

        val file = outputFile.get().asFile
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }

        val content = buildString {
            appendLine("libraries:")
            if (dependencies.isEmpty()) {
                logger.warn("⚠️ WARNING: No dependencies matched the filter! Check your allowedGroups.")
            }
            dependencies.forEach {
                appendLine("  - $it")
            }
        }

        file.writeText(content)
        logger.lifecycle("✅ paper-libraries.yml generated with ${dependencies.size} libraries")
        dependencies.forEach { logger.lifecycle("   -> $it") }
    }
}

tasks.register<GeneratePaperLibrariesTask>("generatePaperLibrariesYaml") {
    group = "build setup"
    description = "Generates paper-libraries.yml from runtime dependencies"

    classpathConfiguration.set(project.configurations.named("runtimeClasspath"))
    outputFile.set(project.layout.projectDirectory.file("src/main/resources/paper-libraries.yml"))

    allowedGroups.set(
        setOf(
            "org.jetbrains.kotlin",
            "org.jetbrains.kotlinx",
            "org.jetbrains.exposed",
            "io.ktor",
            "org.incendo",
            "org.flywaydb",
            "com.zaxxer",
            "org.mariadb.jdbc",
            "org.postgresql",
            "org.xerial",
            "de.exlll",
            "com.tchristofferson",
            "org.apache.commons"
        )
    )
}