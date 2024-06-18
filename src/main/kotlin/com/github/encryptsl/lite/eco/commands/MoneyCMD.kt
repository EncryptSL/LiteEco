package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
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
    fun onBalanceProxy(commandSender: CommandSender, @Argument(value = "player", suggestions = "onlinePlayers") offlinePlayer: OfflinePlayer?) {
        onBalance(commandSender, offlinePlayer)
    }

    @Command("money bal [player]")
    @Permission("lite.eco.balance")
    fun onBalance(commandSender: CommandSender, @Argument(value = "player", suggestions = "onlinePlayers") offlinePlayer: OfflinePlayer?) {
        if (commandSender is Player) {
            val cSender = offlinePlayer ?: commandSender
            liteEco.api.getUserByUUID(cSender).thenApply { user ->
                val formatMessage = when(offlinePlayer) {
                    null -> liteEco.locale.translation("messages.balance.format", helper.getComponentBal(user))
                    else -> liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(user))
                }
                commandSender.sendMessage(formatMessage)
            }.exceptionally {
                commandSender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", cSender.name.toString())))
            }
            return
        }

        if (offlinePlayer != null) {
            liteEco.api.getUserByUUID(offlinePlayer).thenApply { user ->
                commandSender.sendMessage(
                    liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(user))
                )
            }.exceptionally {
                commandSender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                    Placeholder.parsed("account", offlinePlayer.name.toString())))
            }
            return
        }

        liteEco.locale.getList("messages.help")
            ?.forEach { s -> commandSender.sendMessage(ModernText.miniModernText(s.toString())) }
    }

    @ProxiedBy("baltop")
    @Command("money top [page]")
    @Permission("lite.eco.top")
    fun onTopBalance(commandSender: CommandSender, @Argument(value = "page") @Range(min = "1", max="") @Default("1") page: Int) {

        val topPlayers = helper.getTopBalancesFormatted()

        val pagination = ComponentPaginator(topPlayers) { itemsPerPage = 10 }.apply { page(page) }

        if (pagination.isAboveMaxPage(page))
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.maximum_page",
                Placeholder.parsed("max_page", pagination.maxPages.toString()))
            )

        for (content in pagination.display()) {
            commandSender.sendMessage(
                liteEco.locale.translation("messages.balance.top_header", TagResolver.resolver(
                    Placeholder.parsed("page", pagination.currentPage().toString()),
                    Placeholder.parsed("max_page", pagination.maxPages.toString())
                )).appendNewline()
                    .append(content)
                    .appendNewline()
                    .append(liteEco.locale.translation("messages.balance.top_footer")))
        }
    }

    @ProxiedBy("pay")
    @Command("money pay <player> <amount>")
    @Permission("lite.eco.pay")
    fun onPayMoney(
        sender: Player,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String
    ) {
        if (sender.uniqueId == offlinePlayer.uniqueId)
            return sender.sendMessage(liteEco.locale.translation("messages.error.self_pay"))

        val amount = helper.validateAmount(amountStr, sender) ?: return
        liteEco.pluginManager.callEvent(PlayerEconomyPayEvent(sender, offlinePlayer, amount))
    }
}