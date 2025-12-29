package com.github.encryptsl.lite.eco.common.manager.economy

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
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
            liteEco.api.getUserByUUID(target.uniqueId, currency).orElse(null)?.let { user ->
                if (!liteEco.api.has(sender.uniqueId, currency, money))
                    return@launch sender.sendMessage(liteEco.locale.translation("messages.error.insufficient_funds"))

                if (liteEco.currencyImpl.getCheckBalanceLimit(user.money, currency, money)) {
                    sender.sendMessage(liteEco.locale.translation("messages.error.balance_above_limit",
                        Placeholder.parsed("account", target.name.toString())
                    ))
                    return@launch
                }
                liteEco.loggerModel.logging(TypeLogger.TRANSFER,
                    sender.name, target.name.toString(), currency, user.money, user.money.plus(money)
                )
                liteEco.api.transfer(sender.uniqueId, target.uniqueId, currency, money)

                sender.sendMessage(liteEco.locale.translation("messages.sender.add_money", TagResolver.resolver(
                    Placeholder.parsed("target", target.name.toString()),
                    Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))

                liteEco.increaseTransactions(1)

                if (target.isOnline) {
                    target.player?.sendMessage(liteEco.locale.translation("messages.target.add_money", TagResolver.resolver(
                        Placeholder.parsed("sender", sender.name),
                        Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                        Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                    )))
                }
            } ?: sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                Placeholder.parsed("account", target.name.toString())
            ))
        }
    }

}