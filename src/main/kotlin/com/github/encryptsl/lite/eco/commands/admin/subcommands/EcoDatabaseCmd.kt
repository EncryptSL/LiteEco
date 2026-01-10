package com.github.encryptsl.lite.eco.commands.admin.subcommands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.ExportKeys
import com.github.encryptsl.lite.eco.api.enums.PurgeKey
import com.github.encryptsl.lite.eco.commands.EcoCMD
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.commands.parsers.ImportEconomyParser
import com.github.encryptsl.lite.eco.common.manager.ExportManager
import com.github.encryptsl.lite.eco.common.manager.ImportManager
import com.github.encryptsl.lite.eco.common.manager.PurgeManager
import com.github.encryptsl.lite.eco.common.manager.importer.ImportEconomy
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source

class EcoDatabaseCmd(
    private val liteEco: LiteEco,
    private val importEconomy: ImportEconomy,
    private val purgeManager: PurgeManager,
    private val exportManager: ExportManager,
    private val importManager: ImportManager
) : CommandFeature {

    private val importEconomyParser by lazy { ImportEconomyParser(importEconomy) }

    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        val dbBase = base.literal("database")

        commandManager.command(
            dbBase.literal("purge")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.purge")
                .required(
                    commandManager
                        .componentBuilder(PurgeKey::class.java, "argument")
                        .suggestionProvider(commandManager.parserRegistry().getSuggestionProvider("purgeKeys").get())
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val purgeKey: PurgeKey = ctx.get("argument")
                    val currency: String = ctx.get("currency")
                    purgeManager.purge(sender, purgeKey, currency)
                })

        commandManager.command(
            dbBase.literal("export")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.export")
                .required(
                    commandManager
                        .componentBuilder(ExportKeys::class.java, "argument")
                        .suggestionProvider(commandManager.parserRegistry().getSuggestionProvider("exportKeys").get())
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val exportKey: ExportKeys = ctx.get("argument")
                    val currency: String = ctx.get("currency")
                    exportManager.export(sender, exportKey, currency)
                })

        commandManager.command(
            dbBase.literal("import")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.import")
                .required(
                    commandManager
                        .componentBuilder(String::class.java, "economy")
                        .parser(importEconomyParser)
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val economy: String = ctx.get("economy")
                    val currency: String = ctx.get("currency")
                    liteEco.pluginScope.launch {
                        importManager.importEconomy(sender, economy, currency)
                    }
                })
    }
}