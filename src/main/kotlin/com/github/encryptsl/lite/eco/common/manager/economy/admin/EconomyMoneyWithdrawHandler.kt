package com.github.encryptsl.lite.eco.common.manager.economy.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull
import kotlin.time.ExperimentalTime

class EconomyMoneyWithdrawHandler(
    private val liteEco: LiteEco
) {

    @OptIn(ExperimentalTime::class)
    fun onAdminWithdrawMoney(
        sender: CommandSender,
        target: OfflinePlayer,
        currency: String,
        money: BigDecimal,
        silent: Boolean,
    ) {
        liteEco.pluginScope.launch {
            val userOpt = liteEco.api.getUserByUUID(target.uniqueId, currency).getOrNull()
            if (userOpt == null) {
                sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", target.name.toString())
                ))
                return@launch
            }

            if (!liteEco.api.has(target.uniqueId, currency, money)) {
                sender.sendMessage(liteEco.locale.translation("messages.error.insufficient_funds"))
                return@launch
            }

            liteEco.loggerModel.logging(TransactionContextEntity(TypeLogger.WITHDRAW, sender.name, target.name.toString(), currency, userOpt.money, userOpt.money.minus(money)))

            liteEco.increaseTransactions(1)
            liteEco.api.withdraw(target.uniqueId, currency, money)

            if (sender.name == target.name) {
                sender.sendMessage(liteEco.locale.translation("messages.self.withdraw_money",
                    TagResolver.resolver(
                        Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                        Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                    )
                ))
                return@launch
            }
            sender.sendMessage(
                liteEco.locale.translation("messages.sender.withdraw_money",
                    TagResolver.resolver(
                        Placeholder.parsed("target", target.name.toString()),
                        Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                        Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                    )
                )
            )
            if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_withdraw")) {
                if (silent) {
                    target.player?.sendMessage(liteEco.locale.translation("messages.target.withdraw_money_silent",
                        Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                    ))
                    return@launch
                }
                target.player?.sendMessage(liteEco.locale.translation("messages.target.withdraw_money",
                    TagResolver.resolver(
                        Placeholder.parsed("sender", sender.name),
                        Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                        Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                    ))
                )
            }
        }
    }

}