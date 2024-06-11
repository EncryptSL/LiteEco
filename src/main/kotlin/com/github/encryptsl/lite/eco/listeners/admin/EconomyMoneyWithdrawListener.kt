package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyMoneyWithdrawEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyMoneyWithdrawListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyMoneyWithdraw(event: EconomyMoneyWithdrawEvent) {
        val sender: CommandSender = event.commandSender
        val target: OfflinePlayer = event.offlinePlayer
        val money: Double = event.money
        val silent: Boolean = event.silent

        if (!liteEco.api.has(target, money))
            return sender.sendMessage(liteEco.locale.translation("messages.error.insufficient_funds"))

        liteEco.api.getUserByUUID(target).thenApply {
            liteEco.increaseTransactions(1)
            liteEco.api.withDrawMoney(target, money)
            liteEco.loggerModel.info(liteEco.locale.getMessage("messages.monolog.admin.normal.withdraw")
                .replace("<sender>", sender.name)
                .replace("<target>", target.name.toString())
                .replace("<money>", liteEco.api.fullFormatting(money))
            )
        }.exceptionally {
           sender.sendMessage(
                liteEco.locale.translation("messages.error.account_not_exist", Placeholder.parsed("account", target.name.toString())))
        }

        if (sender.name == target.name)
            return sender.sendMessage(
                liteEco.locale.translation("messages.self.withdraw_money", Placeholder.parsed("money", liteEco.api.fullFormatting(money)))
            )

        sender.sendMessage(
                liteEco.locale.translation("messages.sender.withdraw_money",
                TagResolver.resolver(Placeholder.parsed("target", target.name.toString()), Placeholder.parsed("money", liteEco.api.fullFormatting(money)))))

        if (target.isOnline && liteEco.config.getBoolean("messages.target.notify_withdraw")) {
            if (silent) {
                target.player?.sendMessage(
                    liteEco.locale.translation("messages.target.withdraw_money_silent",
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                ))
                return
            }

            target.player?.sendMessage(
                liteEco.locale.translation("messages.target.withdraw_money",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        }
    }

}