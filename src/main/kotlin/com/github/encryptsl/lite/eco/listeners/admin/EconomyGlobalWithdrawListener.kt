package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalWithdrawEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyGlobalWithdrawListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalWithdraw(event: EconomyGlobalWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val currency = event.currency
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
            return sender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", currency)))

        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))

        for (p in offlinePlayers) {
            liteEco.api.hasAccount(p.uniqueId, currency).thenAccept { el ->
                if (el == true) liteEco.api.withDrawMoney(p.uniqueId, currency, money)
            }
        }

        liteEco.increaseTransactions(offlinePlayers.size)
        liteEco.loggerModel.info(liteEco.locale.plainTextTranslation("messages.monolog.admin.global.withdraw", TagResolver.resolver(
            Placeholder.parsed("sender", sender.name),
            Placeholder.parsed("accounts", offlinePlayers.size.toString()),
            Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
        )))

        sender.sendMessage(
            liteEco.locale.translation("messages.global.withdraw_money",
            TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )
        ))
        if (liteEco.config.getBoolean("messages.global.notify_withdraw"))
            Bukkit.broadcast(
                liteEco.locale.translation("messages.broadcast.withdraw_money",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )
            ))
            return
    }
}