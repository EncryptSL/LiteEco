package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.internal.ConfirmationPostprocessor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.ConsoleSource
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

        val confirmationManager = customConfirmationManager()

        commandManager.registerCommandPostProcessor(confirmationManager)

        registerMinecraftExceptionHandler(commandManager)
        registerSuggestionModernProviders(commandManager)

        listOf(
            MoneyCMD(liteEco, confirmationManager),
            EcoCMD(liteEco, confirmationManager),
        ).forEach {
            it.execute(commandManager)
        }
    }

    private fun registerMinecraftExceptionHandler(commandManager: PaperCommandManager<Source>) {
        MinecraftExceptionHandler.create<Source> { source -> source.source() }
            .defaultHandlers()
            .decorator { component ->
                ModernText.miniModernText(liteEco.baseConfig.plugin.prefix).appendSpace().append(component)
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

    private fun customConfirmationManager(): ConfirmationPostprocessor<Source> {
        val confirmationManager = ConfirmationPostprocessor<Source>(
            timeoutSeconds = 15,
            getUuid = { sender ->
                (sender.source() as? Player)?.uniqueId
            },
            hasPermission = { sender, permission ->
                sender.source().hasPermission(permission)
            },
            sendNotification = { sender, context ->
                val fullCommand = context.rawInput().input()
                val message = if (sender.source() is ConsoleCommandSender) {
                    liteEco.locale.translation(
                        "messages.error.command_require_confirm", TagResolver.resolver(
                            Placeholder.parsed("command", fullCommand)
                        )
                    )
                } else {
                    liteEco.locale.translation(
                        "messages.error.command_require_confirm", TagResolver.resolver(
                            Placeholder.parsed("command", "/$fullCommand")
                        )
                    )
                }
                sender.source().sendMessage(message)
            }
        )

        return confirmationManager
    }

    private fun modifiableSuggestionPlayerSuggestion(): CompletableFuture<List<Suggestion>> {
        val suggestion = if (liteEco.baseConfig.plugin.offlineSuggestionPlayers) {
            CompletableFuture.completedFuture(Bukkit.getOfflinePlayers()
                .map { Suggestion.suggestion(it.name.toString()) })
        } else {
            CompletableFuture.completedFuture(Bukkit.getOnlinePlayers().map { Suggestion.suggestion(it.name) })
        }

        return suggestion
    }
}