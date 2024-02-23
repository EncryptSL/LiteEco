package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.events.PlayerEconomyPayEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerEconomyPayListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onEconomyPay(event: PlayerEconomyPayEvent) {
        val sender: Player = event.sender
        val target: OfflinePlayer = event.target
        val money: Double = event.money

        if (!liteEco.api.hasAccount(target))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.account_not_exist"),
                TagResolver.resolver(Placeholder.parsed("account", target.name.toString()))))

        if (!liteEco.api.has(sender, money))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.insufficient_funds")))

        if (liteEco.api.getCheckBalanceLimit(target, money))
            return sender.sendMessage(ModernText.miniModernText(
                liteEco.locale.getMessage("messages.error.balance_above_limit"),
                Placeholder.parsed("account", target.name.toString())
            ))

        liteEco.api.withDrawMoney(sender, money)
        liteEco.api.depositMoney(target, money)
        liteEco.increaseTransactions(1)
        sender.sendMessage(
            ModernText.miniModernText(
                liteEco.locale.getMessage("messages.sender.add_money"),
                TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money)))))
        if (target.isOnline) {
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