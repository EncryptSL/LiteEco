package encryptsl.cekuj.net.listeners.admin

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.admin.EconomyMoneyWithdrawEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyMoneyWithdrawListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyMoneyWithdraw(event: EconomyMoneyWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money
        val silent: Boolean = event.silent

        if (!liteEco.api.hasAccount(target))
            return sender.sendMessage(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))

        if (!liteEco.api.has(target, money))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.insufficient_funds")))

        liteEco.increaseTransactions(1)
        liteEco.api.withDrawMoney(target, money)
        if (sender.name == target.name) {
            return sender.sendMessage(
                ModernText.miniModernText(
                    liteEco.locale.getMessage("messages.self.withdraw_money"),
                    TagResolver.resolver(Placeholder.parsed("money", liteEco.api.fullFormatting(money)))
                )
            )
        }

        sender.sendMessage(
            ModernText.miniModernText(
                liteEco.locale.getMessage("messages.sender.withdraw_money"),
                TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money)))))
        if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_withdraw")) {
            if (silent) {
                target.player?.sendMessage(ModernText.miniModernText(
                    liteEco.locale.getMessage("messages.target.withdraw_money_silent"),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                ))
                return
            }

            target.player?.sendMessage(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.target.withdraw_money"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        }
    }

}