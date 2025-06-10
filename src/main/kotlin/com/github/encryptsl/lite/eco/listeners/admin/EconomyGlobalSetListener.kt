package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalSetEvent
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.jvm.optionals.getOrNull

class EconomyGlobalSetListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalSet(event: EconomyGlobalSetEvent) {
        val sender: CommandSender = event.commandSender
        val currency = event.currency
        val money = event.money
        val players = event.players

        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))


        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit")) {
            sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))
            return
        }

        liteEco.pluginScope.launch {
            players.forEach { player ->
                val user = liteEco.suspendApiWrapper
                    .getUserByUUID(player.uniqueId, currency)
                    .getOrNull()

                user?.also { u ->
                    with(liteEco) {
                        loggerModel.logging(
                            EconomyOperations.SET,
                            sender.name,
                            u.userName,
                            currency,
                            u.money,
                            money
                        )
                        suspendApiWrapper.set(player.uniqueId, currency, money)
                    }
                }
            }

            liteEco.increaseTransactions(players.size)

            sender.sendMessage(liteEco.locale.translation("messages.global.set_money", TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))

            if (liteEco.config.getBoolean("messages.global.notify_set")) {
                Bukkit.broadcast(liteEco.locale.translation("messages.broadcast.set_money", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
            }
        }
    }
}