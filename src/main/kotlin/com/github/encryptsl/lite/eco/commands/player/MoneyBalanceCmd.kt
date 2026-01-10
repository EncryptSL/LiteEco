package com.github.encryptsl.lite.eco.commands.player

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.utils.Helper
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source

class MoneyBalanceCmd(
    private val liteEco: LiteEco,
    private val helper: Helper
) : CommandFeature {
    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        val balBase = base.literal("bal", "balance")
            .permission("lite.eco.balance")

        val playerBal = balBase.senderType(PlayerSource::class.java)
            .handler { ctx ->
                val sender = ctx.sender().source()
                showBalance(sender, sender, liteEco.currencyImpl.defaultCurrency())
            }

        val targetBal = balBase
            .required(
                "target",
                OfflinePlayerParser.offlinePlayerParser(),
                commandManager.parserRegistry().getSuggestionProvider("players").get()
            )
            .optional(
                "currency", commandManager.componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser()).defaultValue(DefaultValue.parsed("dollars"))
            )
            .handler { ctx ->
                val sender = ctx.sender().source()
                val target: OfflinePlayer = ctx.get("target")
                val currency: String = ctx.get("currency")

                if (!sender.hasPermission("lite.eco.balance.others") && sender != target) {
                    sender.sendMessage(liteEco.locale.translation("messages.error.missing_balance_others_permission"))
                    return@handler
                }
                showBalance(sender, target, currency)
            }

        commandManager.command(playerBal)
        commandManager.command(targetBal)

        commandManager.command(commandManager.commandBuilder("bal", "balance").proxies(playerBal.build()))
        commandManager.command(commandManager.commandBuilder("bal", "balance").proxies(targetBal.build()))
    }

    private fun showBalance(sender: CommandSender, target: OfflinePlayer, currency: String) {
        if (!sender.hasPermission("lite.eco.balance.$currency") && !sender.hasPermission("lite.eco.balance.*")) {
            sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))
            return
        }

        liteEco.pluginScope.launch {
            liteEco.api.getUserByUUID(target.uniqueId, currency).ifPresentOrElse({ user ->
                val key = if (sender == target) "messages.balance.format" else "messages.balance.format_target"
                sender.sendMessage(liteEco.locale.translation(key, helper.getComponentBal(user, currency)))
            }, {
                sender.sendMessage(
                    liteEco.locale.translation(
                        "messages.error.account_not_exist",
                        Placeholder.parsed("account", target.name.toString())
                    )
                )
            })
        }
    }
}