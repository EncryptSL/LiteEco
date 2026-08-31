package com.github.encryptsl.lite.eco.common.manager.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.math.BigDecimal

class PlayerEconomyPayHandler(
    private val liteEco: LiteEco
) {

    fun onPlayerPay(
        sender: Player,
        target: OfflinePlayer,
        money: BigDecimal,
        currency: String
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

            val isTransferred = account.transfer(sender.uniqueId, target.uniqueId, currency, money)

            if (!isTransferred) {
                val senderBalance = account.getBalance(sender.uniqueId, currency)
                val targetBalance = account.getBalance(target.uniqueId, currency)

                val errorMessage = when {
                    senderBalance < money ->
                        liteEco.locale.translation("messages.error.insufficient_funds")

                    liteEco.currencyImpl.getCheckBalanceLimit(targetBalance, currency, money) ->
                        liteEco.locale.translation("messages.error.balance_above_limit", Placeholder.parsed("account", targetName))

                    else ->
                        liteEco.locale.translation("messages.error.transfer_failed")
                }

                liteEco.schedulerHelper.sendMessageSync(sender, errorMessage)
                return@launch
            }

            val updatedTargetBalance = account.getBalance(target.uniqueId, currency)
            val previousTargetBalance = updatedTargetBalance.minus(money)

            liteEco.increaseTransactions(1)

            liteEco.loggerModel.logging(
                TransactionContextEntity(
                    TypeLogger.TRANSFER,
                    sender.name,
                    user.userName,
                    currency,
                    previousTargetBalance,
                    updatedTargetBalance
                )
            )

            val formattedMoney = liteEco.currencyImpl.fullFormatting(money, currency)
            val currencyName = liteEco.currencyImpl.currencyModularNameConvert(currency, money)

            val moneyPlaceholders = TagResolver.resolver(
                Placeholder.parsed("money", formattedMoney),
                Placeholder.parsed("currency", currencyName)
            )

            liteEco.schedulerHelper.sendMessageSync(
                sender,
                liteEco.locale.translation(
                    "messages.sender.add_money",
                    TagResolver.resolver(Placeholder.parsed("target", targetName), moneyPlaceholders)
                )
            )

            if (target.isOnline) {
                target.player?.let { targetPlayer ->
                    liteEco.schedulerHelper.sendMessageSync(
                        targetPlayer,
                        liteEco.locale.translation(
                            "messages.target.add_money",
                            TagResolver.resolver(Placeholder.parsed("sender", sender.name), moneyPlaceholders)
                        )
                    )
                }
            }
        }
    }

}