package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalDepositEvent
import com.github.encryptsl.lite.eco.api.objects.ModernText
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

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(ModernText.miniModernText(liteEco.locale.getMessage("messages.error.amount_above_limit")))

        //TODO: I don't know now how solve issue with not checking balance, only one way is add other same function with checking sender permission.
        for (p in offlinePlayers) {
            if (!liteEco.api.hasAccount(p) || liteEco.api.getCheckBalanceLimit(p, money)) continue
            liteEco.api.depositMoney(p, money)
        }

        liteEco.increaseTransactions(offlinePlayers.size)

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