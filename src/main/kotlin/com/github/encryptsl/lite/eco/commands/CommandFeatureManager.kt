package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.objects.ModernText
import org.bukkit.Bukkit
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.suggestion.Suggestion
import java.util.concurrent.CompletableFuture

class CommandFeatureManager(
    private val liteEco: LiteEco,
) {
    internal fun createCommandManager() {
        liteEco.logger.info(ModernText.miniModernText("<blue>Registering commands with Cloud Command Framework !"))

        val commandManager: PaperCommandManager<Source> = PaperCommandManager
            .builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(liteEco)

        registerMinecraftExceptionHandler(commandManager)
        registerSuggestionModernProviders(commandManager)

        listOf(
            MoneyCMD(liteEco),
            EcoCMD(liteEco),
        ).forEach {
            it.execute(commandManager)
        }
    }

    private fun registerMinecraftExceptionHandler(commandManager: PaperCommandManager<Source>) {
        MinecraftExceptionHandler.create<Source> { source -> source.source() }
            .defaultHandlers()
            .decorator { component ->
                ModernText.miniModernText(liteEco.config.getString("plugin.prefix", "<red>[!]").toString()).appendSpace().append(component)
            }.registerTo(commandManager)
    }

    private fun registerSuggestionModernProviders(commandManager: PaperCommandManager<Source>) {
        commandManager.parserRegistry().registerSuggestionProvider("players") { _, _ ->
            modifiableSuggestionPlayerSuggestion()
        }
        commandManager.parserRegistry().registerSuggestionProvider("purgeKeys") { _, _ ->
            CompletableFuture.completedFuture(PurgeKey.entries.map { Suggestion.suggestion(it.name) })
        }
        commandManager.parserRegistry().registerSuggestionProvider("exportKeys") { _, _ ->
            CompletableFuture.completedFuture(ExportKeys.entries.map { Suggestion.suggestion(it.name) })
        }
    }

    private fun modifiableSuggestionPlayerSuggestion(): CompletableFuture<List<Suggestion>> {
        val suggestion = if (liteEco.config.getBoolean("plugin.offline-suggestion-players", true)) {
            CompletableFuture.completedFuture(Bukkit.getOfflinePlayers()
                .map { Suggestion.suggestion(it.name.toString()) })
        } else {
            CompletableFuture.completedFuture(Bukkit.getOnlinePlayers().map { Suggestion.suggestion(it.name) })
        }

        return suggestion
    }
}