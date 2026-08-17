package com.github.encryptsl.lite.eco.common.manager.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.api.errors.CouldNotTransferredException
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
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
        liteEco.pluginScope.launch {
            val targetName = target.name ?: "Unknown"
            val account = liteEco.api.account()
            val user = account.getUserByUUID(target.uniqueId, currency)

            if (user == null) {
                sender.sendMessage(
                    liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", targetName))
                )
                return@launch
            }

            if (!account.has(sender.uniqueId, currency, money)) {
                sender.sendMessage(liteEco.locale.translation("messages.error.insufficient_funds"))
                return@launch
            }

            if (liteEco.currencyImpl.getCheckBalanceLimit(user.money, currency, money)) {
                sender.sendMessage(
                    liteEco.locale.translation("messages.error.balance_above_limit", Placeholder.parsed("account", targetName))
                )
                return@launch
            }
            if(account.transfer(sender.uniqueId, target.uniqueId, currency, money)) {

                val targetNewBalance = user.money.plus(money)
                liteEco.increaseTransactions(1)

                liteEco.loggerModel.logging(
                    TransactionContextEntity(TypeLogger.TRANSFER, sender.name, user.userName, currency, user.money, targetNewBalance)
                )

                val formattedMoney = liteEco.currencyImpl.fullFormatting(money, currency)
                val currencyName = liteEco.currencyImpl.currencyModularNameConvert(currency, money)

                val moneyPlaceholders = TagResolver.resolver(
                    Placeholder.parsed("money", formattedMoney),
                    Placeholder.parsed("currency", currencyName)
                )

                sender.sendMessage(
                    liteEco.locale.translation(
                        "messages.sender.add_money",
                        TagResolver.resolver(Placeholder.parsed("target", targetName), moneyPlaceholders)
                    )
                )

                if (target.isOnline) {
                    target.player?.sendMessage(
                        liteEco.locale.translation(
                            "messages.target.add_money",
                            TagResolver.resolver(Placeholder.parsed("sender", sender.name), moneyPlaceholders)
                        )
                    )
                }
            } else {
                throw CouldNotTransferredException(sender.uniqueId, target.uniqueId, currency, money)
            }
        }
    }

}