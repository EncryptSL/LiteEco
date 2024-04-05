package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneySetEvent
import com.github.encryptsl.lite.eco.api.objects.ModernText
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
            return sender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", target.name.toString())))

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        liteEco.increaseTransactions(1)

        if (sender.name == target.name)
            return sender.sendMessage(liteEco.locale.translation("messages.self.set_money", Placeholder.parsed("money", liteEco.api.fullFormatting(money))))

        liteEco.api.setMoney(target, money)
        liteEco.loggerModel.info("Admin ${sender.name} set player ${target.name} : ${liteEco.api.fullFormatting(money)}")

        sender.sendMessage(
            liteEco.locale.translation("messages.sender.set_money",
            TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money)))))

        if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_set")) {
            target.player?.sendMessage(
                liteEco.locale.translation("messages.target.set_money",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        }
    }

}