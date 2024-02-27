package encryptsl.cekuj.net.listeners.admin

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.admin.EconomyGlobalSetEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyGlobalSetListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalSet(event: EconomyGlobalSetEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.amount_above_limit")))

        for (p in offlinePlayers) {
            if (!liteEco.api.hasAccount(p)) { continue }
            liteEco.api.setMoney(p, money)
        }

        liteEco.increaseTransactions(offlinePlayers.size)

        sender.sendMessage(
            ModernText.miniModernText(liteEco.locale.getMessage("messages.global.set_money"),
            TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money))
            )
        ))
        if (liteEco.config.getBoolean("messages.global.notify_set"))
            Bukkit.broadcast(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.broadcast.set_money"),
                    TagResolver.resolver(
                        Placeholder.parsed("sender", sender.name),
                        Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                    )))
            return
    }
}