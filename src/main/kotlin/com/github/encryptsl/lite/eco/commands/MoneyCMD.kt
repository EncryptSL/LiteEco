package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.api.events.PlayerEconomyPayEvent
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.utils.Helper
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.extension.buildAndRegister
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PlayerSource
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser
import java.util.concurrent.CompletableFuture

class MoneyCMD(
    private val commandManager: PaperCommandManager<Source>,
    private val liteEco: LiteEco
) {

    companion object {
        private const val DESCRIPTION = "Provided plugin by LiteEco"
    }

    private val helper: Helper = Helper(liteEco)

    fun playerCommands() {
        commandManager.buildAndRegister("money", Description.description(DESCRIPTION)) {
            permission("lite.eco.money")
            handler { ctx -> helpMessage(ctx.sender().source()) }

            commandManager.command(commandBuilder.literal("help").permission("lite.eco.money").handler {  ctx ->
                helpMessage(ctx.sender().source())
            })

            val balanceCmd = commandBuilder.literal("bal").permission("lite.eco.balance")
            val playerBalanceCmd = balanceCmd.senderType(PlayerSource::class.java).handler { ctx ->
                val sender: Player = ctx.sender().source()
                balanceCommand(sender, sender, liteEco.currencyImpl.defaultCurrency())
            }
            val consoleBalanceCmd = balanceCmd
                .required(
                    "target",
                    OfflinePlayerParser.offlinePlayerParser(),
                    commandManager.parserRegistry().getSuggestionProvider("players").get()
                )
                .optional(commandManager.componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                )
                .handler { ctx ->
                    val sender: CommandSender = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val currency: String = ctx.getOrDefault("currency", liteEco.currencyImpl.defaultCurrency())
                    if (!sender.hasPermission("lite.eco.balance.others") && sender != target) {
                        return@handler sender.sendMessage(liteEco.locale.translation("messages.error.missing_balance_others_permission"))
                    }
                    balanceCommand(sender, target, currency)
                }
            commandManager.command(playerBalanceCmd)
            commandManager.command(consoleBalanceCmd)

            val balanceProxyCmd = commandManager.commandBuilder("bal", "balance")

            commandManager.command(balanceProxyCmd.proxies(playerBalanceCmd.build()))
            commandManager.command(balanceProxyCmd.proxies(consoleBalanceCmd.build()))

            val balanceTopCmd = commandBuilder.literal("top")
                .permission("lite.eco.top")
                .optional("page", IntegerParser.integerParser(1), DefaultValue.constant(1))
                .optional(
                commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender = ctx.sender().source()
                    val page: Int = ctx.getOrDefault("page", 1)
                    val currency: String = ctx.getOrDefault("currency", "dollars")
                    balanceTopCommand(sender, page, currency)
                }

            commandManager.command(balanceTopCmd)

            commandManager.command(commandManager.commandBuilder("balancetop", "baltop")
                .proxies(balanceTopCmd.build()))

            val payCmd = commandBuilder
                .literal("pay")
                .senderType(PlayerSource::class.java)
                .permission("lite.eco.pay")
                .required("target", OfflinePlayerParser.offlinePlayerParser(), commandManager.parserRegistry().getSuggestionProvider("players").get())
                .required("int", IntegerParser.integerParser(1))
                    .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: Player = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amountStr: Int = ctx.get("int")
                    val currency: String = ctx.get("currency")
                    payCommand(sender, target, amountStr, currency)
                }
            commandManager.command(payCmd)
            commandManager.command(commandManager.commandBuilder("pay").proxies(payCmd.build()))
        }
    }

    fun helpMessage(sender: CommandSender) {
        liteEco.locale.getList("messages.help")?.forEach { s -> sender.sendMessage(ModernText.miniModernText(s.toString())) }
    }

    fun balanceCommand(sender: CommandSender, target: OfflinePlayer, currency: String) {
        if (!sender.hasPermission("lite.eco.balance.$currency") && !sender.hasPermission("lite.eco.balance.*"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))

        liteEco.pluginScope.launch {
            liteEco.suspendApiWrapper.getUserByUUID(target.uniqueId, currency).ifPresentOrElse({
                val formatMessage = if (sender == target)
                    liteEco.locale.translation("messages.balance.format", helper.getComponentBal(it, currency))
                else
                    liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(it, currency))
                sender.sendMessage(formatMessage)
            }, {
                sender.sendMessage(
                    liteEco.locale.translation("messages.error.account_not_exist",
                        Placeholder.parsed("account", target.name.toString()))
                )
            })
        }
    }

    fun balanceTopCommand(sender: CommandSender, page: Int, currency: String) {
        try {
            CompletableFuture.runAsync {
                val topPlayers = helper.getTopBalancesFormatted(currency)

                val pagination = ComponentPaginator(topPlayers) { itemsPerPage = 10 }.apply { page(page) }

                if (pagination.isAboveMaxPage(page))
                    return@runAsync sender.sendMessage(
                        liteEco.locale.translation(
                            "messages.error.maximum_page",
                            Placeholder.parsed("max_page", pagination.maxPages.toString())
                        )
                    )

                val tagResolver = TagResolver.resolver(
                    Placeholder.parsed("page", pagination.currentPage().toString()),
                    Placeholder.parsed("max_page", pagination.maxPages.toString())
                )
                sender.sendMessage(liteEco.locale.translation("messages.balance.top_header", tagResolver))
                for (content in pagination.display()) {
                    sender.sendMessage(content)
                }
                sender.sendMessage(liteEco.locale.translation("messages.balance.top_footer", tagResolver))
            }
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
            e.fillInStackTrace()
        }
    }

    fun payCommand(sender: Player, target: OfflinePlayer, amountStr: Int, currency: String) {
        if (!sender.hasPermission("lite.eco.pay.$currency") && !sender.hasPermission("lite.eco.pay.*"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))

        if (sender.uniqueId == target.uniqueId)
            return sender.sendMessage(liteEco.locale.translation("messages.error.self_pay"))

        val amount = helper.validateAmount(amountStr.toString(), sender) ?: return
        liteEco.pluginManager.callEvent(PlayerEconomyPayEvent(sender, target, currency, amount))
    }

}