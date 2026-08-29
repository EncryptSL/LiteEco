package com.github.encryptsl.lite.eco.common.manager.economy.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.math.BigDecimal

class EconomyMoneyWithdrawHandler(
    private val liteEco: LiteEco
) {

    fun onAdminWithdrawMoney(
        sender: CommandSender,
        target: OfflinePlayer,
        currency: String,
        money: BigDecimal,
        silent: Boolean,
    ) {
        liteEco.pluginScope.launch(Dispatchers.IO) {
            val targetName = target.name ?: "Unknown"
            val account = liteEco.api.account()
            val user = account.getUserByUUID(target.uniqueId, currency)

            if (user == null) {
                liteEco.schedulerHelper.sendMessageSync(
                    sender,
                    liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", targetName))
                )
                return@launch
            }

            if (!account.has(target.uniqueId, currency, money)) {
                liteEco.schedulerHelper.sendMessageSync(
                    sender,
                    liteEco.locale.translation("messages.error.insufficient_funds")
                )
                return@launch
            }

            val newBalance = user.money.minus(money)
            liteEco.increaseTransactions(1)

            liteEco.loggerModel.logging(
                TransactionContextEntity(TypeLogger.WITHDRAW, sender.name, user.userName, currency, user.money, newBalance)
            )
            account.withdraw(target.uniqueId, currency, money)

            val formattedMoney = liteEco.currencyImpl.fullFormatting(money, currency)
            val currencyName = liteEco.currencyImpl.currencyModularNameConvert(currency, money)

            val moneyPlaceholders = TagResolver.resolver(
                Placeholder.parsed("money", formattedMoney),
                Placeholder.parsed("currency", currencyName)
            )

            // Dispatch messages to sender safely via SchedulerHelper
            if (sender.name == targetName) {
                liteEco.schedulerHelper.sendMessageSync(
                    sender,
                    liteEco.locale.translation("messages.self.withdraw_money", moneyPlaceholders)
                )
                return@launch
            }

            liteEco.schedulerHelper.sendMessageSync(
                sender,
                liteEco.locale.translation(
                    "messages.sender.withdraw_money",
                    TagResolver.resolver(Placeholder.parsed("target", targetName), moneyPlaceholders)
                )
            )

            if (target.isOnline && liteEco.baseConfig.messages.target.notifyWithdraw) {
                val targetPlayer = target.player ?: return@launch

                val targetMsg = if (silent) {
                    liteEco.locale.translation("messages.target.withdraw_money_silent", moneyPlaceholders)
                } else {
                    liteEco.locale.translation(
                        "messages.target.withdraw_money",
                        TagResolver.resolver(Placeholder.parsed("sender", sender.name), moneyPlaceholders)
                    )
                }

                liteEco.schedulerHelper.sendMessageSync(targetPlayer, targetMsg)
            }
        }
    }

}