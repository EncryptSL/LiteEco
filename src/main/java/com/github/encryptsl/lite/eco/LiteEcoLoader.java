package com.github.encryptsl.lite.eco;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class LiteEcoLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder pluginClasspath) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolveLibraries().stream()
                .map(DefaultArtifact::new)
                .forEach(artifact -> resolver.addDependency(new Dependency(artifact, null)));

        resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
        pluginClasspath.addLibrary(resolver);
    }

    private List<String> resolveLibraries() {
        try {
            return readLibraryListFromYaml();
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        return new ArrayList<>();
    }

    private List<String> readLibraryListFromYaml() throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream = LiteEcoLoader.class.getClassLoader()
                .getResourceAsStream("paper-libraries.yml");

        if (inputStream == null) {
            System.err.println("paper-libraries.yml not found in the classpath.");
        }

        Map<String, List<String>> data = yaml.load(inputStream);

        return data.get("libraries");
    }
}
