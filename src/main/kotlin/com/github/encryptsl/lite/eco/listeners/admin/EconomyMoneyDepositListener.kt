package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.economy.EconomyOperations
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneyDepositEvent
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull

class EconomyMoneyDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyDeposit(event: EconomyMoneyDepositEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val currency = event.currency
        val money: BigDecimal = event.money
        val silent: Boolean = event.silent

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit")) {
            sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))
            return
        }

        liteEco.pluginScope.launch {
            val user = liteEco.suspendApiWrapper.getUserByUUID(target.uniqueId, currency).getOrNull()
            if (user == null) {
                sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", target.name.toString())))
                return@launch
            }
            if (liteEco.api.getCheckBalanceLimit(target.uniqueId, user.money, currency, money) && !sender.hasPermission("lite.eco.admin.bypass.limit")) {
                sender.sendMessage(liteEco.locale.translation("messages.error.balance_above_limit",
                    Placeholder.parsed("account", target.name.toString())
                ))
                return@launch
            }

            liteEco.increaseTransactions(1)
            liteEco.loggerModel.logging(EconomyOperations.DEPOSIT, sender.name, user.userName, currency, user.money, user.money.plus(money))
            liteEco.suspendApiWrapper.deposit(target.uniqueId, currency, money)

            if (sender.name == target.name) {
                sender.sendMessage(liteEco.locale.translation("messages.self.add_money", TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
                return@launch
            }

            sender.sendMessage(liteEco.locale.translation("messages.sender.add_money",
                TagResolver.resolver(
                    Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )
            ))

            if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_add")) {
                if (silent) {
                    target.player?.sendMessage(liteEco.locale.translation(
                        "messages.target.add_money_silent",
                        Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency))
                    ))
                    return@launch
                }
                target.player?.sendMessage(liteEco.locale.translation("messages.target.add_money", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money)))
                ))
            }
        }

    }

}