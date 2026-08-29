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

class EconomyMoneySetHandler(
    private val liteEco: LiteEco
) {

    fun onAdminSetMoney(
        sender: CommandSender,
        target: OfflinePlayer,
        currency: String,
        money: BigDecimal,
    ) {
        val isPlayerBypassing = (sender is Player) && sender.hasPermission("lite.eco.admin.bypass.limit")

        // Synchronous validation before launching async coroutine
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

            liteEco.increaseTransactions(1)
            liteEco.loggerModel.logging(
                TransactionContextEntity(TypeLogger.SET, sender.name, user.userName, currency, user.money, money)
            )
            account.set(target.uniqueId, currency, money)

            val formattedMoney = liteEco.currencyImpl.fullFormatting(money, currency)
            val currencyName = liteEco.currencyImpl.currencyModularNameConvert(currency, money)

            val moneyPlaceholders = TagResolver.resolver(
                Placeholder.parsed("money", formattedMoney),
                Placeholder.parsed("currency", currencyName)
            )

            if (sender.name == targetName) {
                liteEco.schedulerHelper.sendMessageSync(
                    sender,
                    liteEco.locale.translation("messages.self.set_money", moneyPlaceholders)
                )
                return@launch
            }

            liteEco.schedulerHelper.sendMessageSync(
                sender,
                liteEco.locale.translation(
                    "messages.sender.set_money",
                    TagResolver.resolver(Placeholder.parsed("target", targetName), moneyPlaceholders)
                )
            )

            if (target.isOnline && liteEco.baseConfig.messages.target.notifySet) {
                val targetPlayer = target.player ?: return@launch
                liteEco.schedulerHelper.sendMessageSync(
                    targetPlayer,
                    liteEco.locale.translation(
                        "messages.target.set_money",
                        TagResolver.resolver(Placeholder.parsed("sender", sender.name), moneyPlaceholders)
                    )
                )
            }
        }
    }

}