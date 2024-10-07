package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneyDepositEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.math.BigDecimal

class EconomyMoneyDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyDeposit(event: EconomyMoneyDepositEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val currency = event.currency
        val money: BigDecimal = event.money
        val silent: Boolean = event.silent

        if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
            return sender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", currency)))

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        if (liteEco.api.getCheckBalanceLimit(target.uniqueId, currency, money) || !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(
                liteEco.locale.translation("messages.error.balance_above_limit",
                    Placeholder.parsed("account", target.name.toString())
                ))

        liteEco.api.getUserByUUID(target.uniqueId, currency).thenAccept {
            liteEco.increaseTransactions(1)
            liteEco.api.depositMoney(target.uniqueId, currency, money)
            liteEco.loggerModel.info(liteEco.locale.plainTextTranslation("messages.monolog.admin.normal.deposit", TagResolver.resolver(
                Placeholder.parsed("sender", sender.name),
                Placeholder.parsed("target", target.name.toString()),
                Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))
        }.exceptionally {
            sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", target.name.toString())))
            return@exceptionally null
        }

        if (sender.name == target.name) {
            return sender.sendMessage(
                liteEco.locale.translation("messages.self.add_money", TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )
            ))
        }

        sender.sendMessage(liteEco.locale.translation("messages.sender.add_money",
            TagResolver.resolver(
                Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )
        ))

        if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_add")) {
            if (silent) {
                target.player?.sendMessage(
                    liteEco.locale.translation("messages.target.add_money_silent", Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency))
                ))
                return
            }

            target.player?.sendMessage(
                liteEco.locale.translation("messages.target.add_money",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )
            ))
        }
    }

}