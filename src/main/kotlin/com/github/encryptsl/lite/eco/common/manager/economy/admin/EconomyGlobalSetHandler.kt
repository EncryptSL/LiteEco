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
import org.bukkit.entity.Player
import java.math.BigDecimal

class EconomyGlobalSetHandler(
    private val liteEco: LiteEco
) {

    fun onAdminGlobalSetMoney(
        sender: CommandSender,
        currency: String,
        money: BigDecimal,
        players: MutableCollection<OfflinePlayer>
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
            if (liteEco.api.getUUIDNameMap(currency).isEmpty()) {
                liteEco.schedulerHelper.sendMessageSync(
                    sender,
                    liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !"))
                )
                return@launch
            }

            val account = liteEco.api.account()
            for (player in players) {
                val user = account.getUserByUUID(player.uniqueId, currency) ?: continue

                liteEco.loggerModel.logging(
                    TransactionContextEntity(
                        type = TypeLogger.SET,
                        sender = sender.name,
                        target = user.userName,
                        currency = currency,
                        previousBalance = user.money,
                        newBalance = money
                    )
                )

                account.set(user.uuid, currency, money)
            }

            liteEco.increaseTransactions(players.size)

            val moneyPlaceholders = TagResolver.resolver(
                Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )

            liteEco.schedulerHelper.sendMessageSync(
                sender,
                liteEco.locale.translation("messages.global.set_money", moneyPlaceholders)
            )

            if (liteEco.baseConfig.messages.global.notifySet) {
                val broadcastMsg = liteEco.locale.translation(
                    "messages.broadcast.set_money",
                    TagResolver.resolver(
                        Placeholder.parsed("sender", sender.name),
                        moneyPlaceholders
                    )
                )

                liteEco.schedulerHelper.runSyncNow {
                    Bukkit.broadcast(broadcastMsg)
                }
            }
        }
    }

}