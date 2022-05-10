package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.events.ConsoleEconomyTransactionEvent
import encryptsl.cekuj.net.api.objects.ModernText
import encryptsl.cekuj.net.extensions.isNegative
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.milkbowl.vault.economy.EconomyResponse
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
            val economyResponse: EconomyResponse? = liteEco.econ?.depositPlayer(target, money)
            if (economyResponse?.transactionSuccess() == true) {
                if (sender.name == target.name) {
                    sender.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.self_add_money"), TagResolver.resolver(Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                    return
                }
                sender.sendMessage(ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.sender_of_pay"),
                    TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                if (target.isOnline) {
                    target.player?.sendMessage(ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.target_of_pay"),
                        TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                }
            } else {
                sender.sendMessage(ModernText.miniModernText(economyResponse!!.errorMessage))
            }
            return
        }

        if (event.transactionType == TransactionType.WITHDRAW) {
            val economyResponse: EconomyResponse? = liteEco.econ?.withdrawPlayer(target, money)
            if (economyResponse?.transactionSuccess() == true) {
                if (sender.name == target.name) {
                    sender.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.self_withdraw_money"), TagResolver.resolver(Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                    return
                }
                sender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.sender_success_withdraw"),
                        TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                if (target.isOnline) {
                    target.player?.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.target_success_withdraw"),
                            TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                }
            } else {
                sender.sendMessage(ModernText.miniModernText(economyResponse!!.errorMessage))
            }
            return
        }

        if (event.transactionType == TransactionType.SET) {
            if (liteEco.econ?.hasAccount(target) == true) {
                if (money.isNegative()) {
                    sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.negative_amount_error")))
                    return
                }
                liteEco.preparedStatements.setMoney(target.uniqueId, money)
                if (sender.name == target.name) {
                    sender.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.self_set_money"), TagResolver.resolver(Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                    return
                }
                sender.sendMessage(ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.sender_success_set"),
                    TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                if (target.isOnline) {
                    target.player?.sendMessage(ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.target_success_set"),
                        TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", liteEco.econ!!.format(money)))))
                }
            } else {
                sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.account_not_exist"),
                    TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            }
            return
        }
    }

}