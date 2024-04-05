package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.Paginator
import com.github.encryptsl.lite.eco.api.events.PlayerEconomyPayEvent
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.utils.Helper
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotation.specifier.Range
import org.incendo.cloud.annotations.*

@Suppress("UNUSED")
@CommandDescription("Provided plugin by LiteEco")
class MoneyCMD(private val liteEco: LiteEco) {
    private val helper: Helper = Helper(liteEco)

    @Command("money help")
    @Permission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.locale.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @Command("bal|balance [player]")
    @Permission("lite.eco.balance")
    fun onBalanceProxy(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        onBalance(commandSender, offlinePlayer)
    }

    @Command("money bal [player]")
    @Permission("lite.eco.balance")
    fun onBalance(commandSender: CommandSender, @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?) {
        if (commandSender is Player) {
            val cSender = offlinePlayer ?: commandSender

            val formatMessage = when(offlinePlayer) {
                null -> liteEco.locale.translation("messages.balance.format", helper.getComponentBal(commandSender))
                else -> liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(offlinePlayer))
            }

            if (!liteEco.api.hasAccount(cSender))
                return commandSender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", cSender.name.toString())))

            commandSender.sendMessage(formatMessage)
        } else {
           offlinePlayer?.let {
               if (!liteEco.api.hasAccount(it))
                   return commandSender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                       Placeholder.parsed("account", it.name.toString())))

              return commandSender.sendMessage(
                   liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(offlinePlayer))
               )
            }

            liteEco.locale.getList("messages.help")?.forEach { s ->
                commandSender.sendMessage(ModernText.miniModernText(s.toString()))
            }
        }
    }

    @ProxiedBy("baltop")
    @Command("money top [page]")
    @Permission("lite.eco.top")
    fun onTopBalance(commandSender: CommandSender, @Argument(value = "page") @Range(min = "1", max="") page: Int?) {
        val p = page ?: 1

        val topPlayers = helper.getTopBalancesFormatted()
        if (topPlayers.isEmpty()) return

        val pagination = Paginator(topPlayers).apply { page(p) }
        val isPageAboveMaxPages = p > pagination.maxPages

        if (isPageAboveMaxPages)
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", pagination.maxPages.toString()))
            )

        commandSender.sendMessage(
                liteEco.locale.translation("messages.balance.top_header",
                TagResolver.resolver(
                    Placeholder.parsed("page", pagination.page().toString()), Placeholder.parsed("max_page", pagination.maxPages.toString())
                ))
                .appendNewline().append(ModernText.miniModernText(pagination.display()))
                .appendNewline().append(liteEco.locale.translation("messages.balance.top_footer"))
        )
    }

    @ProxiedBy("pay")
    @Command("money pay <player> <amount>")
    @Permission("lite.eco.pay")
    fun onPayMoney(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        if (commandSender !is Player)
            return commandSender.sendMessage(ModernText.miniModernText("<red>Only a player can use this command."))

        if (commandSender.name == offlinePlayer.name)
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.self_pay"))

        val amount = helper.validateAmount(amountStr, commandSender) ?: return
        liteEco.pluginManager.callEvent(PlayerEconomyPayEvent(commandSender, offlinePlayer, amount))
    }
}