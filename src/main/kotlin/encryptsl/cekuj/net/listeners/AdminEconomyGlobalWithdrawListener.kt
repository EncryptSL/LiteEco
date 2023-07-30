package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.AdminEconomyGlobalWithdrawEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class AdminEconomyGlobalWithdrawListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalWithdraw(event: AdminEconomyGlobalWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        offlinePlayers.filter { p -> liteEco.api.hasAccount(p) }.forEach { a ->
            liteEco.api.withDrawMoney(a, money)
        }

        liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + 1

        sender.sendMessage(
            ModernText.miniModernText(liteEco.locale.getMessage("messages.sender_global_withdraw"),
            TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.formatting(money))
            )
        ))

        if (!liteEco.config.getBoolean("plugin.disableMessages.g_broadcast_withdraw")) {
            Bukkit.broadcast(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.g_broadcast_withdraw"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.formatting(money))
                )
            ))
        }
    }
}