package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneyWithdrawEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.math.BigDecimal

class EconomyMoneyWithdrawListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyMoneyWithdraw(event: EconomyMoneyWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val currency = event.currency
        val money: BigDecimal = event.money
        val silent: Boolean = event.silent

        if (!liteEco.api.has(target.uniqueId, currency, money))
            return sender.sendMessage(liteEco.locale.translation("messages.error.insufficient_funds"))


        liteEco.api.getUserByUUID(target.uniqueId, currency).thenAccept {
            if (!it.isPresent) {
                sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", target.name.toString())
                ))
                return@thenAccept
            }
            val user = it.get()
            liteEco.loggerModel.logging(EconomyOperations.WITHDRAW, sender.name, target.name.toString(), currency, user.money, user.money.minus(money))

            liteEco.increaseTransactions(1)
            liteEco.api.withDrawMoney(target, currency, money)

            if (sender.name == target.name) {
                sender.sendMessage(liteEco.locale.translation("messages.self.withdraw_money", TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
                return@thenAccept
            }

            sender.sendMessage(liteEco.locale.translation("messages.sender.withdraw_money", TagResolver.resolver(
                Placeholder.parsed("target", target.name.toString()),
                Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))

            if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_withdraw")) {
                if (silent) {
                    target.player?.sendMessage(
                        liteEco.locale.translation("messages.target.withdraw_money_silent",
                            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                        ))
                    return@thenAccept
                }

                target.player?.sendMessage(liteEco.locale.translation("messages.target.withdraw_money", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
            }
        }
    }

}