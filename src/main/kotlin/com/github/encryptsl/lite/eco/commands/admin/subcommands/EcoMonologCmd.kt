package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologManager
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser
import org.incendo.cloud.parser.standard.StringParser

class EcoMonologCmd(
    private val monologManager: MonologManager
) : CommandFeature {
    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        commandManager.command(
            base.literal("monolog")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.monolog")
                .optional("page", IntegerParser.integerParser(1), DefaultValue.constant(1))
                .optional(
                    "target",
                    StringParser.stringParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val page: Int = ctx.get("page")
                    val target: String = ctx.getOrDefault("target", "all")
                    monologManager.displayMonolog(sender, target, page)
                })
    }
}