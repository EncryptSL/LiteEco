package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.PlayerEconomyPayEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.math.BigDecimal

class PlayerEconomyPayListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onEconomyPay(event: PlayerEconomyPayEvent) {
        val sender: Player = event.sender
        val target: OfflinePlayer = event.target
        val money: BigDecimal = event.money
        val currency: String = event.currency

        if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
            return sender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", currency)))

        if (!liteEco.api.has(sender.uniqueId, currency, money))
            return sender.sendMessage(liteEco.locale.translation("messages.error.insufficient_funds"))

        if (liteEco.api.getCheckBalanceLimit(target.uniqueId, currency, money))
            return sender.sendMessage(
                liteEco.locale.translation("messages.error.balance_above_limit",
                    Placeholder.parsed("account", target.name.toString())
            ))

        liteEco.api.getUserByUUID(target.uniqueId).thenAccept { user ->
            sender.sendMessage(liteEco.locale.translation("messages.sender.add_money", TagResolver.resolver(
                Placeholder.parsed("target", user.userName),
                Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))
        }.thenApply {
            liteEco.api.transfer(sender.uniqueId, target.uniqueId, currency, money)
            liteEco.loggerModel.info(liteEco.locale.plainTextTranslation("messages.monolog.player.pay", TagResolver.resolver(
                Placeholder.parsed("sender", sender.name),
                Placeholder.parsed("target", target.name.toString()),
                Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))
            liteEco.increaseTransactions(1)
        }.exceptionally {
            val error = liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", target.name.toString()))
            return@exceptionally sender.sendMessage(error)
        }

        if (target.isOnline) {
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