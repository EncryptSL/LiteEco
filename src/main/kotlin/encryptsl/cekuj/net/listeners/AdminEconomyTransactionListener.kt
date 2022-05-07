package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.events.AdminEconomyTransactionEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyTransactionListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyTransaction(event: AdminEconomyTransactionEvent) {
        val sender: Player = event.player
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money
        if (event.transactionType == TransactionType.ADD) {
            val economyResponse: EconomyResponse? = liteEco.econ?.depositPlayer(target, money)
            if (economyResponse?.transactionSuccess() == true) {
                if (sender.player?.name == target.name) {
                    sender.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.self_add_money"), TagResolver.resolver(Placeholder.parsed("money", money.toString()))))
                    return
                }

                sender.sendMessage(
                    ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.sender_success_pay"),
                    TagResolver.resolver(Placeholder.parsed("target", target.player?.name.toString()), Placeholder.parsed("money", money.toString()))))
                if (target.isOnline) {
                    target.player?.sendMessage(
                        ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.target_success_pay"),
                        TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", money.toString()))))
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
                            liteEco.translationConfig.getMessage("messages.self_withdraw_money"), TagResolver.resolver(Placeholder.parsed("money", money.toString()))))
                    return
                }
                sender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.sender_success_withdraw"),
                        TagResolver.resolver(Placeholder.parsed("target", target.player?.name.toString()), Placeholder.parsed("money", money.toString()))))
                if (target.isOnline) {
                    target.player?.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.target_success_withdraw"),
                            TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", money.toString()))))
                }
            } else {
                sender.sendMessage(ModernText.miniModernText(economyResponse!!.errorMessage))
            }
            return
        }

        if (event.transactionType == TransactionType.SET) {
            if (liteEco.econ?.hasAccount(target) == true) {
                if (sender.player?.name == target.name) {
                    sender.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.self_set_money"), TagResolver.resolver(Placeholder.parsed("money", money.toString()))))
                    return
                }
                sender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.sender_of_set"),
                        TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", money.toString()))))
                if (target.isOnline) {
                    target.player?.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.target_of_set"),
                            TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", money.toString()))))
                }
            } else {
                sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.account_not_exist"),
                    TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            }
            return
        }

    }

}