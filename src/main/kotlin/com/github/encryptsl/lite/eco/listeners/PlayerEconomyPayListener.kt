package com.github.encryptsl.lite.eco.listeners

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.api.events.PlayerEconomyPayEvent
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull

class PlayerEconomyPayListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onEconomyPay(event: PlayerEconomyPayEvent) {
        val sender: Player = event.sender
        val target: OfflinePlayer = event.target
        val money: BigDecimal = event.money
        val currency: String = event.currency

        liteEco.pluginScope.launch {
            val user = liteEco.suspendApiWrapper.getUserByUUID(target.uniqueId, currency).getOrNull()
            if (user == null) {
                sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", target.name.toString())
                ))
                return@launch
            }

            if (!liteEco.api.has(sender.uniqueId, currency, money))
                return@launch sender.sendMessage(liteEco.locale.translation("messages.error.insufficient_funds"))

            if (liteEco.api.getCheckBalanceLimit(target.uniqueId, user.money, currency, money)) {
                sender.sendMessage(liteEco.locale.translation("messages.error.balance_above_limit",
                    Placeholder.parsed("account", target.name.toString())
                ))
                return@launch
            }

            liteEco.loggerModel.logging(TypeLogger.TRANSFER,
                sender.name, target.name.toString(), currency, user.money, user.money.plus(money)
            )
            liteEco.suspendApiWrapper.transfer(sender.uniqueId, target.uniqueId, currency, money)
        }

        sender.sendMessage(liteEco.locale.translation("messages.sender.add_money", TagResolver.resolver(
            Placeholder.parsed("target", target.name.toString()),
            Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
            Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
        )))

        liteEco.increaseTransactions(1)

        if (target.isOnline) {
            target.player?.sendMessage(liteEco.locale.translation("messages.target.add_money", TagResolver.resolver(
                Placeholder.parsed("sender", sender.name),
                Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))
        }
    }
}