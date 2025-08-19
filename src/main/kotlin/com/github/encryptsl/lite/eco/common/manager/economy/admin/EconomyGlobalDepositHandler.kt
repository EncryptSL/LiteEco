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

class EconomyGlobalDepositHandler(
    private val liteEco: LiteEco
) {

    fun onAdminGlobalDepositMoney(
        sender: CommandSender,
        currency: String,
        money: BigDecimal,
        players:  MutableCollection<OfflinePlayer>
    ) {
        if (liteEco.api.getUUIDNameMap(currency).isEmpty())
            return sender.sendMessage(liteEco.locale.translation("messages.error.database_exception", Placeholder.parsed("exception", "Collection is empty !")))

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit")) {
            sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))
            return
        }
        liteEco.pluginScope.launch {
            players.forEach { player ->
                val user = liteEco.suspendApiWrapper
                    .getUserByUUID(player.uniqueId, currency)
                    .getOrNull()

                user?.takeIf { u ->
                    !liteEco.api.getCheckBalanceLimit(u.uuid, u.money, currency, money)
                }?.also { u ->
                    with(liteEco) {
                        loggerModel.logging(
                            TypeLogger.DEPOSIT,
                            sender.name,
                            u.userName,
                            currency,
                            u.money,
                            u.money + money
                        )
                        suspendApiWrapper.deposit(u.uuid, currency, money)
                    }
                }
            }
            liteEco.increaseTransactions(players.size)

            sender.sendMessage(
                liteEco.locale.translation("messages.global.add_money", TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )
                ))
            if (liteEco.config.getBoolean("messages.global.notify_add")) {
                Bukkit.broadcast(liteEco.locale.translation("messages.broadcast.add_money", TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money, currency)),
                    Placeholder.parsed("currency", liteEco.currencyImpl.currencyModularNameConvert(currency, money))
                )))
            }
        }
    }

}