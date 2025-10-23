package com.github.encryptsl.lite.eco.common.manager.economy.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import com.github.encryptsl.lite.eco.common.extensions.mainThread
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull

class EconomyGlobalWithdrawHandler(
    private val liteEco: LiteEco
) {

    fun onAdminGlobalWithdrawMoney(
        sender: CommandSender,
        currency: String,
        money: BigDecimal,
        players: MutableCollection<OfflinePlayer>
    ) {
        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))

        liteEco.pluginScope.launch {
            players.forEach { player ->
                val user = liteEco.api
                    .getUserByUUID(player.uniqueId, currency)
                    .getOrNull()
                user?.also { u ->
                    with(liteEco) {
                        loggerModel.logging(
                            TypeLogger.WITHDRAW,
                            sender.name,
                            u.userName,
                            currency,
                            u.money,
                            u.money - money
                        )
                        api.withdraw(u.uuid, currency, money)
                    }
                }
            }

            liteEco.increaseTransactions(players.size)

            mainThread(liteEco) {
                sender.sendMessage(liteEco.locale.translation("messages.global.withdraw_money",
                    TagResolver.resolver(
                        Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money)),
                        Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                    )
                ))
                if (liteEco.config.getBoolean("messages.global.notify_withdraw")) {
                    Bukkit.broadcast(liteEco.locale.translation("messages.broadcast.withdraw_money", TagResolver.resolver(
                        Placeholder.parsed("sender", sender.name),
                        Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                        Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                    )))
                }
            }
        }
    }

}