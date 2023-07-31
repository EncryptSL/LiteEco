package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.AdminEconomyMoneyDepositEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyMoneyDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyDeposit(event: AdminEconomyMoneyDepositEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (!liteEco.api.hasAccount(target)) {
            sender.sendMessage(ModernText.miniModernText(liteEco.translationConfig.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            return
        }
        liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1
        liteEco.api.depositMoney(target, money)
        if (sender.name == target.name) {
            sender.sendMessage(
                ModernText.miniModernText(
                    liteEco.translationConfig.getMessage("messages.error.self_pay"), TagResolver.resolver(Placeholder.parsed("money", liteEco.api.formatting(money)))))
            return
        }

        sender.sendMessage(ModernText.miniModernText(
            liteEco.translationConfig.getMessage("messages.sender.add_money"),
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.formatting(money)))))
        if (target.isOnline) {
            if (liteEco.config.getBoolean("plugin.disableMessages.target_success_pay")) return
            target.player?.sendMessage(ModernText.miniModernText(
                liteEco.translationConfig.getMessage("messages.target.add_money"),
                TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", liteEco.api.formatting(money)))))
        }
    }

}