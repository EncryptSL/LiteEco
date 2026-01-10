package com.github.encryptsl.lite.eco.commands.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser

class EcoAccountManagementCmd(
    private val liteEco: LiteEco,
) : CommandFeature {
    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        commandManager.command(
            base.literal("create")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.create")
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .required(
                    "amount",
                    IntegerParser.integerParser(1)
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Int = ctx.get("amount")
                    val currency: String = ctx.get("currency")

                    liteEco.pluginScope.launch {
                        val message = if (liteEco.api.createOrUpdateAccount(
                                target.uniqueId,
                                target.name.toString(),
                                currency,
                                amountStr.toBigDecimal()
                            )
                        ) {
                            "messages.admin.create_account"
                        } else {
                            "messages.error.account_now_exist"
                        }
                        sender.sendMessage(
                            liteEco.locale.translation(
                                message,
                                Placeholder.parsed("account", target.name.toString())
                            )
                        )
                    }
                })

        commandManager.command(
            base.literal("delete")
                .commandDescription(Description.description(DESCRIPTION))
                .permission("lite.eco.admin.delete")
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .optional(
                    commandManager
                        .componentBuilder(String::class.java, "currency")
                        .parser(CurrencyParser())
                        .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val currency: String = ctx.get("currency")

                    liteEco.pluginScope.launch {
                        val message = if (liteEco.api.deleteAccount(target.uniqueId, currency)) {
                            "messages.admin.delete_account"
                        } else {
                            "messages.error.account_not_exist"
                        }
                        sender.sendMessage(
                            liteEco.locale.translation(
                                message,
                                Placeholder.parsed("account", target.name.toString())
                            )
                        )
                    }
                })
    }
}