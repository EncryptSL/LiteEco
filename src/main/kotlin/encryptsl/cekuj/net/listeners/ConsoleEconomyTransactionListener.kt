package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.events.ConsoleEconomyTransactionEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ConsoleEconomyTransactionListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onConsoleEconomyTransaction(event: ConsoleEconomyTransactionEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (event.transactionType == TransactionType.ADD) {
            liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1
            liteEco.api.depositMoney(target, liteEco.api.getBalance(target).plus(money))
            if (sender.name == target.name) {
                sender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.self_add_money"), TagResolver.resolver(Placeholder.parsed("money", liteEco.econ.format(money)))))
                return
            }
            sender.sendMessage(ModernText.miniModernText(
                liteEco.translationConfig.getMessage("messages.sender_success_pay"),
                TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.econ.format(money)))))
            if (target.isOnline) {
                if (liteEco.config.getBoolean("plugin.disableMessages.target_success_pay")) return
                target.player?.sendMessage(ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.target_success_pay"),
                    TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", liteEco.econ.format(money)))))
            }
            return
        }

        if (event.transactionType == TransactionType.WITHDRAW) {

            liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1
            liteEco.api.withDrawMoney(target, money)
            if (sender.name == target.name) {
                sender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.self_withdraw_money"),
                        TagResolver.resolver(Placeholder.parsed("money", liteEco.econ.format(money)))
                    )
                )
                return
            }

            sender.sendMessage(
                ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.sender_success_withdraw"),
                    TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.econ.format(money)))))
            if (target.isOnline) {
                if (liteEco.config.getBoolean("plugin.disableMessages.target_success_withdraw")) return
                target.player?.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.target_success_withdraw"),
                        TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", liteEco.econ.format(money)))))
            }
            return
        }

        if (event.transactionType == TransactionType.SET) {
            if (!liteEco.econ.hasAccount(target)) {
                sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.account_not_exist"),
                    TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
                return
            }

            liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1
            liteEco.api.setMoney(target, money)
            if (sender.name == target.name) {
                sender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.self_set_money"), TagResolver.resolver(Placeholder.parsed("money", liteEco.econ.format(money)))))
                return
            }
            sender.sendMessage(ModernText.miniModernText(
                liteEco.translationConfig.getMessage("messages.sender_success_set"),
                TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.econ.format(money)))))
            if (target.isOnline) {
                if (liteEco.config.getBoolean("plugin.disableMessages.target_success_set")) return
                target.player?.sendMessage(ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.target_success_set"),
                    TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", liteEco.econ.format(money)))))
            }
            return
        }
    }

}