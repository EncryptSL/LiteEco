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
            return sender.sendMessage(liteEco.locale.translation("messages.error.currencies_not_exist", Placeholder.parsed("currency", currency)))

        for (p in offlinePlayers) {
            liteEco.api.hasAccount(p, currency).thenAccept { el ->
                if (el == true) liteEco.api.withDrawMoney(p, currency, money)
            }
        }

        liteEco.increaseTransactions(offlinePlayers.size)
        liteEco.loggerModel.info(liteEco.locale.getMessage("messages.monolog.admin.global.withdraw")
            .replace("<sender>", sender.name)
            .replace("<accounts>", offlinePlayers.size.toString())
            .replace("<money>", liteEco.api.fullFormatting(money))
        )

        sender.sendMessage(
            liteEco.locale.translation("messages.global.withdraw_money",
            TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money))
            )
        ))
        if (liteEco.config.getBoolean("messages.global.notify_withdraw"))
            Bukkit.broadcast(
                liteEco.locale.translation("messages.broadcast.withdraw_money",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
            return
    }
}