package encryptsl.cekuj.net.listeners.admin

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.admin.EconomyMoneyDepositEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyMoneyDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyDeposit(event: EconomyMoneyDepositEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money
        val silent: Boolean = event.silent

        if (!liteEco.api.hasAccount(target)) {
            sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))
            return
        }

        if (liteEco.api.getCheckBalanceLimit(money) || !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.amount_above_limit")))

        if (liteEco.api.getCheckBalanceLimit(target, money) || !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(
                ModernText.miniModernText(
                    liteEco.locale.getMessage("messages.error.balance_above_limit"),
                    Placeholder.parsed("account", target.name.toString())
                )
            )

        liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1

        liteEco.api.depositMoney(target, money)
        if (sender.name == target.name && !target.isOp) {
            sender.sendMessage(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.error.self_pay"), TagResolver.resolver(Placeholder.parsed("money", liteEco.api.fullFormatting(money)))))
            return
        }

        sender.sendMessage(ModernText.miniModernText(
            liteEco.locale.getMessage("messages.sender.add_money"),
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money)))
        ))
        if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_add")) {
            if (silent) {
                target.player?.sendMessage(ModernText.miniModernText(
                    liteEco.locale.getMessage("messages.target.add_money_silent"),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                ))
                return
            }

            target.player?.sendMessage(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.target.add_money"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        }
    }

}