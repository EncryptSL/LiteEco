package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalDepositEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyGlobalDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyGlobalDeposit(event: EconomyGlobalDepositEvent) {
        val sender: CommandSender = event.commandSender
        val currency = event.currency
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
            return sender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", currency)))

        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        //TODO: I don't know now how solve issue with not checking balance, only one way is add other same function with checking permission.
        for (p in offlinePlayers) {
            liteEco.api.hasAccount(p.uniqueId, currency).thenAccept { el ->
                if (liteEco.api.getCheckBalanceLimit(p.uniqueId, liteEco.api.getBalance(p.uniqueId), currency, money)) {
                    if (el == true) {
                        liteEco.api.depositMoney(p, currency, money)
                    }
                }
            }
        }

        liteEco.increaseTransactions(offlinePlayers.size)
        liteEco.loggerModel.info(liteEco.locale.plainTextTranslation("messages.monolog.admin.global.deposit", TagResolver.resolver(
            Placeholder.parsed("sender", sender.name),
            Placeholder.parsed("accounts", offlinePlayers.size.toString()),
            Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
        )))

        sender.sendMessage(
            liteEco.locale.translation("messages.global.add_money", TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )
        ))
        if (liteEco.config.getBoolean("messages.global.notify_add")) {
            Bukkit.broadcast(
                liteEco.locale.translation("messages.broadcast.add_money",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )
            ))
        }
    }

}