package com.github.encryptsl.lite.eco.common.manager.economy.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.database.entity.TransactionContextEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.math.BigDecimal

class EconomyGlobalWithdrawHandler(
    private val liteEco: LiteEco
) {

    fun onAdminGlobalWithdrawMoney(
        sender: CommandSender,
        currency: String,
        money: BigDecimal,
        players: MutableCollection<OfflinePlayer>
    ) {
        liteEco.pluginScope.launch(Dispatchers.IO) {
            val account = liteEco.api.account()

            if (liteEco.api.getUUIDNameMap(currency).isEmpty()) {
                liteEco.schedulerHelper.sendMessageSync(
                    sender,
                    liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !"))
                )
                return@launch
            }

            for (player in players) {
                val user = account.getUserByUUID(player.uniqueId, currency) ?: continue

                if (user.money < money) {
                    continue
                }

                val newBalance = user.money.minus(money)

                liteEco.loggerModel.logging(
                    TransactionContextEntity(
                        type = TypeLogger.WITHDRAW,
                        sender = sender.name,
                        target = user.userName,
                        currency = currency,
                        previousBalance = user.money,
                        newBalance = newBalance
                    )
                )

                account.withdraw(user.uuid, currency, money)
            }

            liteEco.increaseTransactions(players.size)

            val moneyPlaceholders = TagResolver.resolver(
                Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )

            liteEco.schedulerHelper.sendMessageSync(
                sender,
                liteEco.locale.translation("messages.global.withdraw_money", moneyPlaceholders)
            )

            if (liteEco.baseConfig.messages.global.notifyWithdraw) {
                val broadcastMsg = liteEco.locale.translation(
                    "messages.broadcast.withdraw_money",
                    TagResolver.resolver(Placeholder.parsed("sender", sender.name), moneyPlaceholders)
                )

                liteEco.schedulerHelper.runSyncNow {
                    Bukkit.broadcast(broadcastMsg)
                }
            }
        }
    }

}