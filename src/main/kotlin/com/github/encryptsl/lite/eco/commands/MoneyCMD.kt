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
import java.util.concurrent.CompletableFuture

@Suppress("UNUSED")
@CommandDescription("Provided plugin by LiteEco")
class MoneyCMD(private val liteEco: LiteEco) {
    private val helper: Helper = Helper(liteEco)

    @Command("money")
    @Permission("lite.eco.money")
    fun onRootCommand(commandSender: CommandSender) {
        onHelp(commandSender)
    }

    @Command("money help")
    @Permission("lite.eco.help")
    fun onHelp(commandSender: CommandSender) {
        liteEco.locale.getList("messages.help")?.forEach { s ->
            commandSender.sendMessage(ModernText.miniModernText(s.toString()))
        }
    }

    @Command("bal|balance [player] [currency]")
    @Permission("lite.eco.balance")
    fun onBalanceProxy(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String?
    ) {
        val c = currency ?: liteEco.currencyImpl.defaultCurrency()
        onBalance(commandSender, offlinePlayer, c)
    }

    @Command("money bal [player] [currency]")
    @Permission("lite.eco.balance")
    fun onBalance(
        commandSender: CommandSender,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer?,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String?
    ) {
        val c = currency ?: liteEco.currencyImpl.defaultCurrency()

        if (!liteEco.currencyImpl.getCurrencyNameExist(c))
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", c)))

        if (!commandSender.hasPermission("lite.eco.balance.$c") && !commandSender.hasPermission("lite.eco.balance.*"))
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))

        if (commandSender is Player) {
            val cSender = offlinePlayer ?: commandSender
            liteEco.api.getUserByUUID(cSender.uniqueId, c).thenAccept { value ->
                value.ifPresentOrElse({
                    val formatMessage = when(offlinePlayer) {
                        null -> liteEco.locale.translation("messages.balance.format", helper.getComponentBal(it, c))
                        else -> liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(it, c))
                    }
                    commandSender.sendMessage(formatMessage)
                }, {
                    commandSender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                        Placeholder.parsed("account", cSender.name.toString())))
                })
            }
            return
        }

        if (offlinePlayer != null) {
            liteEco.api.getUserByUUID(offlinePlayer.uniqueId, c).thenAccept { user ->
                user.ifPresentOrElse({
                    commandSender.sendMessage(
                        liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(it, c))
                    )
                }, {
                    commandSender.sendMessage(liteEco.locale.translation("messages.error.account_not_exist",
                        Placeholder.parsed("account", offlinePlayer.name.toString())))
                })
            }
            return
        }

        liteEco.locale.getList("messages.help")
            ?.forEach { s -> commandSender.sendMessage(ModernText.miniModernText(s.toString())) }
    }

    @ProxiedBy("baltop")
    @Command("money top [page] [currency]")
    @Permission("lite.eco.top")
    fun onTopBalance(
        commandSender: CommandSender,
        @Argument(value = "page") @Range(min = "1", max="") @Default("1") page: Int,
        @Argument("currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        if (!liteEco.currencyImpl.getCurrencyNameExist(currency))
            return commandSender.sendMessage(liteEco.locale.translation("messages.error.currency_not_exist", Placeholder.parsed("currency", currency)))

        try {
            CompletableFuture.runAsync({
                val topPlayers = helper.getTopBalancesFormatted(currency)

                val pagination = ComponentPaginator(topPlayers) { itemsPerPage = 10 }.apply { page(page) }

                if (pagination.isAboveMaxPage(page))
                    return@runAsync commandSender.sendMessage(liteEco.locale.translation("messages.error.maximum_page",
                        Placeholder.parsed("max_page", pagination.maxPages.toString()))
                    )

                val tagResolver = TagResolver.resolver(
                    Placeholder.parsed("page", pagination.currentPage().toString()),
                    Placeholder.parsed("max_page", pagination.maxPages.toString())
                )
                commandSender.sendMessage(liteEco.locale.translation("messages.balance.top_header", tagResolver))
                for (content in pagination.display()) {
                    commandSender.sendMessage(content)
                }
                commandSender.sendMessage(liteEco.locale.translation("messages.balance.top_footer", tagResolver))
            })
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
            e.fillInStackTrace()
        }
    }

    @ProxiedBy("pay")
    @Command("money pay <player> <amount> [currency]")
    @Permission("lite.eco.pay")
    fun onPayMoney(
        sender: Player,
        @Argument(value = "player", suggestions = "players") offlinePlayer: OfflinePlayer,
        @Argument(value = "amount") @Range(min = "1.00", max = "") amountStr: String,
        @Argument(value = "currency", suggestions = "currencies") @Default("dollars") currency: String
    ) {
        if (!sender.hasPermission("lite.eco.pay.$currency") && !sender.hasPermission("lite.eco.pay.*"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))

        if (sender.uniqueId == offlinePlayer.uniqueId)
            return sender.sendMessage(liteEco.locale.translation("messages.error.self_pay"))

        val amount = helper.validateAmount(amountStr, sender) ?: return
        liteEco.pluginManager.callEvent(PlayerEconomyPayEvent(sender, offlinePlayer, currency, amount))
    }
}