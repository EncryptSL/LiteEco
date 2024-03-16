package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalWithdrawEvent
import com.github.encryptsl.lite.eco.api.objects.ModernText
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyGlobalWithdrawListener(private val liteEco: LiteEco) : Listener {
    @EventHandler
    fun onAdminEconomyGlobalWithdraw(event: EconomyGlobalWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        for (p in offlinePlayers) {
            if (!liteEco.api.hasAccount(p)) continue
            liteEco.api.withDrawMoney(p, money)
        }

        liteEco.increaseTransactions(offlinePlayers.size)

        sender.sendMessage(
            ModernText.miniModernText(liteEco.locale.getMessage("messages.global.withdraw_money"),
            TagResolver.resolver(
                Placeholder.parsed("money", liteEco.api.fullFormatting(money))
            )
        ))
        if (liteEco.config.getBoolean("messages.global.notify_withdraw"))
            Bukkit.broadcast(
                ModernText.miniModernText(liteEco.locale.getMessage("messages.broadcast.withdraw_money"),
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        return
    }
}