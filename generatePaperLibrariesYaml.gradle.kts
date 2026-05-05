tasks.register("generatePaperLibrariesYaml") {
    group = "build setup"
    description = "Generates paper-libraries.yml from runtime dependencies filtering by allowed groups/prefixes"

    val outputFile = project.layout.projectDirectory.file("src/main/resources/paper-libraries.yml").asFile

    // Používáme runtimeClasspath, protože tam jsou skutečné závislosti potřebné pro běh
    val runtimeClasspath = project.configurations.named("runtimeClasspath")

    // Definujeme povolené skupiny nebo prefixy.
    // Pokud ID skupiny začíná tímto řetězcem, bude zahrnuta.
    val allowedGroups = setOf(
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
        "org.apache.commons",
    )

    // Přidáme závislosti, které by se nemusely chytit přes skupinu (výjimečné případy)
    val allowedSpecificDependencies = setOf(
        "org.slf4j:slf4j-api" // Příklad, pokud bys potřeboval
    )

    inputs.files(runtimeClasspath).withPropertyName("runtimeClasspath")
    outputs.file(outputFile).withPropertyName("outputFile")

    doLast {
        val configuration = runtimeClasspath.get()

        // Resolvujeme tranzitivní závislosti
        val dependencies = configuration.resolvedConfiguration.resolvedArtifacts
            .map { it.moduleVersion.id }
            .filter { id ->
                allowedGroups.any { group -> id.group.startsWith(group) }
            }
            .map { id -> "${id.group}:${id.name}:${id.version}" }
            .distinct()
            .sorted()

        val content = buildString {
            appendLine("libraries:")
            if (dependencies.isEmpty()) {
                println("⚠️ WARNING: No dependencies matched the filter! Check your allowedGroups.")
            }
            dependencies.forEach {
                appendLine("  - $it")
            }
        }

        if (!outputFile.parentFile.exists()) {
            outputFile.parentFile.mkdirs()
        }

        outputFile.writeText(content)
        println("✅ paper-libraries.yml generated with ${dependencies.size} libraries")

        dependencies.forEach { println("   -> $it") }
    }
}