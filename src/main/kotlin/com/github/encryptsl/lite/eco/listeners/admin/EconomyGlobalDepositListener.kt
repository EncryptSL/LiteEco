package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalDepositEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import kotlin.jvm.optionals.getOrNull

class EconomyGlobalDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyGlobalDeposit(event: EconomyGlobalDepositEvent) {
        val sender: CommandSender = event.commandSender
        val currency = event.currency
        val money = event.money
        val players = event.players

        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        liteEco.pluginScope.launch {
            players.forEach { player ->
                val user = liteEco.suspendApiWrapper
                    .getUserByUUID(player.uniqueId, currency)
                    .getOrNull()

                user?.takeIf { u ->
                    !liteEco.api.getCheckBalanceLimit(u.uuid, u.money, currency, money)
                }?.also { u ->
                    with(liteEco) {
                        loggerModel.logging(
                            EconomyOperations.DEPOSIT,
                            sender.name,
                            u.userName,
                            currency,
                            u.money,
                            u.money + money
                        )
                        suspendApiWrapper.deposit(u.uuid, currency, money)
                    }
                }
            }
        }

        liteEco.increaseTransactions(players.size)

        sender.sendMessage(
            liteEco.locale.translation("messages.global.add_money", TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )
        ))
        if (liteEco.config.getBoolean("messages.global.notify_add")) {
            Bukkit.broadcast(liteEco.locale.translation("messages.broadcast.add_money", TagResolver.resolver(
                Placeholder.parsed("sender", sender.name),
                Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))
        }
    }

}