package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalSetEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyGlobalSetListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalSet(event: EconomyGlobalSetEvent) {
        val sender: CommandSender = event.commandSender
        val currency = event.currency
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
            return sender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", currency)))

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        for (p in offlinePlayers) {
            liteEco.api.hasAccount(p, currency).thenAccept { el ->
                if (el == true) liteEco.api.setMoney(p, currency, money)
            }
        }

        liteEco.increaseTransactions(offlinePlayers.size)
        liteEco.loggerModel.info(liteEco.locale.plainTextTranslation("messages.monolog.admin.global.set", TagResolver.resolver(
            Placeholder.parsed("sender", sender.name),
            Placeholder.parsed("accounts", offlinePlayers.size.toString()),
            Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
        )))

        sender.sendMessage(
            liteEco.locale.translation("messages.global.set_money", TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )
        ))

        if (liteEco.config.getBoolean("messages.global.notify_set"))
            Bukkit.broadcast(liteEco.locale.translation("messages.broadcast.set_money", TagResolver.resolver(
                Placeholder.parsed("sender", sender.name),
                Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))
    }
}