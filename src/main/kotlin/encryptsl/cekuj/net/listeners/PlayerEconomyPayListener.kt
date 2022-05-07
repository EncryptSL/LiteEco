package encryptsl.cekuj.net.listeners

import encryptsl.cekuj.net.LiteEco
import encryptsl.cekuj.net.api.enums.TransactionType
import encryptsl.cekuj.net.api.events.PlayerEconomyPayEvent
import encryptsl.cekuj.net.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.milkbowl.vault.economy.EconomyResponse
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

        if (event.transactionType == TransactionType.PAY) {
            val economyResponse: EconomyResponse? = liteEco.econ?.depositPlayer(target.player, money)
            if (liteEco.econ?.has(sender.player, money) == true && economyResponse?.transactionSuccess() == true) {
                liteEco.econ?.depositPlayer(sender, money)
                sender.sendMessage(
                    ModernText.miniModernText(
                        liteEco.translationConfig.getMessage("messages.sender_success_pay"),
                        TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", money.toString()))))
                if (target.isOnline) {
                    target.player?.sendMessage(
                        ModernText.miniModernText(
                            liteEco.translationConfig.getMessage("messages.target_success_pay"),
                            TagResolver.resolver(Placeholder.parsed("sender", sender.name), Placeholder.parsed("money", money.toString()))))
                }
            }
        }
    }
}