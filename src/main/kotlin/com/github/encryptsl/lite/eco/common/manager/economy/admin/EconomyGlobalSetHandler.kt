package com.github.encryptsl.lite.eco.common.manager.economy.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.enums.TypeLogger
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import java.math.BigDecimal
import kotlin.jvm.optionals.getOrNull

class EconomyGlobalSetHandler(
    private val liteEco: LiteEco
) {

    fun onAdminGlobalSetMoney(
        sender: CommandSender,
        currency: String,
        money: BigDecimal,
        players: MutableCollection<OfflinePlayer>
    ) {
        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))


        if (liteEco.currencyImpl.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit")) {
            sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))
            return
        }

        liteEco.pluginScope.launch {
            players.forEach { player ->
                val user = liteEco.api
                    .getUserByUUID(player.uniqueId, currency)
                    .getOrNull()

                user?.also { u ->
                    with(liteEco) {
                        loggerModel.logging(
                            TypeLogger.SET,
                            sender.name,
                            u.userName,
                            currency,
                            u.money,
                            money
                        )
                        api.set(player.uniqueId, currency, money)
                    }
                }
            }

            liteEco.increaseTransactions(players.size)

            sender.sendMessage(liteEco.locale.translation("messages.global.set_money", TagResolver.resolver(
                Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money, currency)),
                Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
            )))

            if (liteEco.config.getBoolean("messages.global.notify_set")) {
                Bukkit.broadcast(liteEco.locale.translation("messages.broadcast.set_money", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.currencyImpl.fullFormatting(money)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
            }
        }
    }

}