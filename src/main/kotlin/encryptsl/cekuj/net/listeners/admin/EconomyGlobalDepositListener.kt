package encryptsl.cekuj.net.listeners.admin

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.admin.EconomyGlobalDepositEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyGlobalDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyGlobalDeposit(event: EconomyGlobalDepositEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        offlinePlayers.filter { p -> liteEco.api.hasAccount(p) }.forEach { a ->
            liteEco.api.depositMoney(a, money)
        }

        liteEco.countTransactions["transactions"] = liteEco.countTransactions.getOrDefault("transactions", 0) + offlinePlayers.size

        sender.sendMessage(
            ModernText.miniModernText(liteEco.locale.getMessage("messages.global.add_money"),
            TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money))
            )
        ))
        if (!liteEco.config.getBoolean("messages.global.notify_add")) {
            Bukkit.broadcast(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.broadcast.add_money"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        }
    }

}