package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.MonologFilterParser
import com.github.encryptsl.lite.eco.common.manager.monolog.MonologManager
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.component.DefaultValue
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
                .permission("lite.eco.admin.monolog")
                .optional("page", IntegerParser.integerParser(1), DefaultValue.constant(1))
                .optional(
                    commandManager.componentBuilder(String::class.java, "filter")
                        .parser(StringParser.quotedStringParser())
                        .suggestionProvider(MonologFilterParser())
                )
                .handler { ctx ->
                    val sender = extractSender(ctx.sender()) ?: return@handler
                    val page: Int = ctx.get("page")
                    val filter: String = ctx.getOrDefault("filter", "")

                    val filteredQuery = MonologFilterParser.parseFromString(filter)

                    monologManager.displayMonolog(sender, filteredQuery, page)
                }
        )
    }

    private fun extractSender(senderObj: Any?): CommandSender? {
        if (senderObj == null) return null
        if (senderObj is CommandSender) return senderObj
        return try {
            senderObj.javaClass.getMethod("source").invoke(senderObj) as? CommandSender
        } catch (_: Exception) {
            null
        }
    }
}