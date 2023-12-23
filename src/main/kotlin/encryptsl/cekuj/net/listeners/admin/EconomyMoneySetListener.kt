package encryptsl.cekuj.net.listeners.admin

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.admin.EconomyMoneySetEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyMoneySetListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyMoneySet(event: EconomyMoneySetEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money

        if (!liteEco.api.hasAccount(target))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.account_not_exist"), TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))

        if (liteEco.api.getCheckBalanceLimit(money) || !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.amount_above_limit")))

        liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1

        liteEco.api.setMoney(target, money)
        if (sender.name == target.name) {
            sender.sendMessage(
                ModernText.miniModernText(
                    liteEco.locale.getMessage("messages.self.set_money"), TagResolver.resolver(Placeholder.parsed("money", liteEco.api.fullFormatting(money)))))
            return
        }

        sender.sendMessage(
            ModernText.miniModernText(
            liteEco.locale.getMessage("messages.sender.set_money"),
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money)))))

        if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_set")) {
            target.player?.sendMessage(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.target.set_money"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        }
    }

}