package com.github.encryptsl.lite.eco.commands.player

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.AmountValidatorParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.common.manager.economy.PlayerEconomyPayHandler
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.incendo.cloud.Command
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import java.math.BigDecimal

class MoneyPayCmd(
    private val liteEco: LiteEco,
    private val economyPay: PlayerEconomyPayHandler,
) : CommandFeature {
    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        val pay = base.literal("pay")
            .senderType(PlayerSource::class.java)
            .permission("lite.eco.pay")
            .required(
                "target",
                OfflinePlayerParser.offlinePlayerParser(),
                commandManager.parserRegistry().getSuggestionProvider("players").get()
            )
            .required(
                "amount",
                commandManager.componentBuilder(BigDecimal::class.java, "amount").parser(AmountValidatorParser())
            )
            .optional(
                "currency",
                commandManager.componentBuilder(String::class.java, "currency").parser(CurrencyParser()).defaultValue(
                    DefaultValue.parsed("dollars")
                )
            )
            .handler { ctx ->
                val sender = ctx.sender().source()
                val target: OfflinePlayer = ctx.get("target")
                val amount: BigDecimal = ctx.get("amount")
                val currency: String = ctx.get("currency")

                if (sender.uniqueId == target.uniqueId) {
                    sender.sendMessage(liteEco.locale.translation("messages.error.self_pay"))
                    return@handler
                }

                if (!sender.hasPermission("lite.eco.pay.$currency") && !sender.hasPermission("lite.eco.pay.*")) {
                    sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))
                    return@handler
                }

                payCommand(sender, target, amount, currency)
            }

        commandManager.command(pay)
        commandManager.command(commandManager.commandBuilder("pay").proxies(pay.build()))
    }

    fun payCommand(sender: Player, target: OfflinePlayer, amount: BigDecimal, currency: String) {
        if (!sender.hasPermission("lite.eco.pay.$currency") && !sender.hasPermission("lite.eco.pay.*"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))

        if (sender.uniqueId == target.uniqueId)
            return sender.sendMessage(liteEco.locale.translation("messages.error.self_pay"))

        economyPay.onPlayerPay(sender, target, amount, currency)
    }
}