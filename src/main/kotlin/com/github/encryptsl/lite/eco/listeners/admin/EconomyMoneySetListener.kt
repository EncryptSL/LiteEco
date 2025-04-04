package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneySetEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.math.BigDecimal

class EconomyMoneySetListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyMoneySet(event: EconomyMoneySetEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val currency = event.currency
        val money: BigDecimal = event.money

        if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
            return sender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", currency)))

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        liteEco.api.getUserByUUID(target.uniqueId).thenAccept {
            if (!it.isPresent) {
                sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", target.name.toString())))
                return@thenAccept
            }
            val user = it.get()

            liteEco.loggerModel.logging(EconomyOperations.SET, sender.name, target.name.toString(), currency, user.money, money)
            liteEco.increaseTransactions(1)
            liteEco.api.setMoney(target.uniqueId, currency, money)

            if (sender.name == target.name) {
                sender.sendMessage(liteEco.locale.translation("messages.self.set_money", TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
                return@thenAccept
            }

            sender.sendMessage(
                liteEco.locale.translation("messages.sender.set_money", TagResolver.resolver(
                    Placeholder.parsed("target", target.name.toString()),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                ))
            )

            if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_set")) {
                target.player?.sendMessage(
                    liteEco.locale.translation("messages.target.set_money",
                        TagResolver.resolver(
                            Placeholder.parsed("sender", sender.name),
                            Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                        )
                    ))
            }
        }
    }

}