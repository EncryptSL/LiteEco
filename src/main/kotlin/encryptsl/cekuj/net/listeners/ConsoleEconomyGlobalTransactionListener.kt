package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.events.ConsoleEconomyGlobalTransactionEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConsoleEconomyGlobalTransactionListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onGlobalTransaction(event: ConsoleEconomyGlobalTransactionEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        if (event.transactionType == TransactionType.GLOBAL_ADD) {

            offlinePlayers.filter { p -> liteEco.econ.hasAccount(p) }.forEach { a ->
                liteEco.api.depositMoney(a, liteEco.api.getBalance(a).plus(money))
            }

            liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + offlinePlayers.size

            sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.sender_global_add"),
                TagResolver.resolver(
                    Placeholder.parsed("money", liteEco.api.formatting(money))
                )
            ))
            if (!liteEco.config.getBoolean("plugin.disableMessages.g_broadcast_pay")) {
                Bukkit.broadcast(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.g_broadcast_add"),
                        TagResolver.resolver(
                            Placeholder.parsed("sender", sender.name),
                            Placeholder.parsed("money", liteEco.api.formatting(money))
                        )
                ))
            }
        }

        if (event.transactionType == TransactionType.GLOBAL_WITHDRAW) {

            offlinePlayers.filter { p -> liteEco.econ.hasAccount(p) }.forEach { a ->
                liteEco.api.withDrawMoney(a, liteEco.api.getBalance(a).minus(money))
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
            offlinePlayers.filter { a -> liteEco.api.hasAccount(a) }.forEach { offlinePlayer ->
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