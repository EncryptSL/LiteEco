package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.events.ConsoleEconomyGlobalTransactionEvent
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.isNegative
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConsoleEconomyGlobalTransactionListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onGlobalTransaction(event: ConsoleEconomyGlobalTransactionEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        var economyResponse: EconomyResponse? = null
        val offlinePlayers = Bukkit.getOfflinePlayers()

        if (event.transactionType == TransactionType.GLOBAL_ADD) {

            offlinePlayers.forEach { a ->
                economyResponse = liteEco.econ.depositPlayer(a, money)
            }

            if (economyResponse?.transactionSuccess() == false) {
                sender.sendMessage(ModernText.miniModernText(economyResponse!!.errorMessage))
                return
            }
            liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + offlinePlayers.size

            sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.sender_global_add"),
                TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.econ.format(money))
                )
            ))
            if (!liteEco.config.getBoolean("plugin.disableMessages.g_broadcast_pay")) {
                Bukkit.broadcast(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.g_broadcast_add"),
                        TagResolver.resolver(
                            Placeholder.parsed("sender", sender.name),
                            Placeholder.parsed("money", liteEco.econ.format(money))
                        )
                ))
            }
        }

        if (event.transactionType == TransactionType.GLOBAL_WITHDRAW) {
            Bukkit.getOfflinePlayers().forEach { a ->
                economyResponse = liteEco.econ.withdrawPlayer(a, money)
            }

            if (economyResponse?.transactionSuccess() == false) {
                sender.sendMessage(ModernText.miniModernText(economyResponse!!.errorMessage))
                return
            }
            liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1

            sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.sender_global_withdraw"),
                TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.econ.format(money))
                )
            ))

            if (!liteEco.config.getBoolean("plugin.disableMessages.g_broadcast_withdraw")) {
                Bukkit.broadcast(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.g_broadcast_withdraw"),
                        TagResolver.resolver(
                            Placeholder.parsed("sender", sender.name),
                            Placeholder.parsed("money", liteEco.econ.format(money))
                        )
                ))
            }

            return
        }

        if (event.transactionType == TransactionType.GLOBAL_SET) {

            if (money.isNegative()) {
                sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
                return
            }

            offlinePlayers.filter { a -> liteEco.econ.hasAccount(a) }.forEach { offlinePlayer ->
                liteEco.preparedStatements.setMoney(offlinePlayer.uniqueId, money)
            }

            liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + offlinePlayers.size

            sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.sender_global_set"),
                TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.econ.format(money))
                )
            ))

            if (!liteEco.config.getBoolean("plugin.disableMessages.g_broadcast_set")) {
                Bukkit.broadcast(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.g_broadcast_set"),
                        TagResolver.resolver(
                            Placeholder.parsed("sender", sender.name),
                            Placeholder.parsed("money", liteEco.econ.format(money))
                        )
                ))
            }
            return
        }
    }

}