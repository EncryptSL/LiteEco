package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalWithdrawEvent
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.jvm.optionals.getOrNull

class EconomyGlobalWithdrawListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalWithdraw(event: EconomyGlobalWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val currency = event.currency
        val money = event.money
        val players = event.players

        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))

        liteEco.pluginScope.launch {
            players.forEach { player ->
                val user = liteEco.suspendApiWrapper
                    .getUserByUUID(player.uniqueId, currency)
                    .getOrNull()
                user?.also { u ->
                    with(liteEco) {
                        loggerModel.logging(
                            EconomyOperations.WITHDRAW,
                            sender.name,
                            u.userName,
                            currency,
                            u.money,
                            u.money - money
                        )
                        suspendApiWrapper.withdraw(u.uuid, currency, money)
                    }
                }
            }

            liteEco.increaseTransactions(players.size)

            sender.sendMessage(liteEco.locale.translation("messages.global.withdraw_money",
                TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )
            ))
            if (liteEco.config.getBoolean("messages.global.notify_withdraw")) {
                Bukkit.broadcast(liteEco.locale.translation("messages.broadcast.withdraw_money", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
            }
        }
    }
}