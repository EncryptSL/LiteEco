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
import org.bukkit.entity.Player
import java.math.BigDecimal

class EconomyMoneyDepositHandler(
    private val liteEco: LiteEco
) {

    fun onAdminDepositMoney(
        sender: CommandSender,
        target: OfflinePlayer,
        currency: String,
        money: BigDecimal,
        silent: Boolean,
    ) {
        val isPlayerBypassing = (sender is Player) && sender.hasPermission("lite.eco.admin.bypass.limit")

        if (liteEco.currencyImpl.getCheckBalanceLimit(money) && !isPlayerBypassing) {
            liteEco.schedulerHelper.sendMessageSync(
                sender,
                liteEco.locale.translation("messages.error.amount_above_limit")
            )
            return
        }

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

            if (liteEco.currencyImpl.getCheckBalanceLimit(user.money, currency, money) && !isPlayerBypassing) {
                liteEco.schedulerHelper.sendMessageSync(
                    sender,
                    liteEco.locale.translation("messages.error.balance_above_limit", Placeholder.parsed("account", targetName))
                )
                return@launch
            }

            val newBalance = user.money.plus(money)
            liteEco.increaseTransactions(1)

            liteEco.loggerModel.logging(
                TransactionContextEntity(TypeLogger.DEPOSIT, sender.name, user.userName, currency, user.money, newBalance)
            )
            account.deposit(target.uniqueId, currency, money)

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
                    liteEco.locale.translation("messages.self.add_money", moneyPlaceholders)
                )
                return@launch
            }

            liteEco.schedulerHelper.sendMessageSync(
                sender,
                liteEco.locale.translation(
                    "messages.sender.add_money",
                    TagResolver.resolver(Placeholder.parsed("target", targetName), moneyPlaceholders)
                )
            )

            if (target.isOnline && liteEco.baseConfig.messages.target.notifyAdd) {
                val targetPlayer = target.player ?: return@launch

                val targetMsg = if (silent) {
                    liteEco.locale.translation("messages.target.add_money_silent", Placeholder.parsed("money", formattedMoney))
                } else {
                    liteEco.locale.translation(
                        "messages.target.add_money",
                        TagResolver.resolver(Placeholder.parsed("sender", sender.name), moneyPlaceholders)
                    )
                }

                liteEco.schedulerHelper.sendMessageSync(targetPlayer, targetMsg)
            }
        }
    }

}