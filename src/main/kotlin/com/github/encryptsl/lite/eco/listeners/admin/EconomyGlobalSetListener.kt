package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalSetEvent
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
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        for (p in offlinePlayers) {
            if (!liteEco.api.hasAccount(p)) continue
            liteEco.api.setMoney(p, money)
        }

        liteEco.increaseTransactions(offlinePlayers.size)
        liteEco.loggerModel.info("Admin ${sender.name} set ${liteEco.api.fullFormatting(money)} to ${offlinePlayers.size} accounts")

        sender.sendMessage(
            liteEco.locale.translation("messages.global.set_money", Placeholder.parsed("money", liteEco.api.fullFormatting(money))
        ))

        if (liteEco.config.getBoolean("messages.global.notify_set"))
            Bukkit.broadcast(
                liteEco.locale.translation("messages.broadcast.set_money",
                    TagResolver.resolver(
                        Placeholder.parsed("sender", sender.name),
                        Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                    )))
    }
}