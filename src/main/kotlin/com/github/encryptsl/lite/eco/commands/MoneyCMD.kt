package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.commands.parsers.AmountValidatorParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.common.extensions.io
import com.github.encryptsl.lite.eco.common.manager.economy.PlayerEconomyPayHandler
import com.github.encryptsl.lite.eco.utils.Helper
import kotlinx.coroutines.launch
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
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
import java.math.BigDecimal

class MoneyCMD(
    private val liteEco: LiteEco
) : InternalCmd {

    companion object {
        private const val DESCRIPTION = "Provided plugin by LiteEco"
    }

    private val helper: Helper = Helper(liteEco)

    private val economyPay by lazy { PlayerEconomyPayHandler(liteEco) }

    override fun execute(commandManager: PaperCommandManager<Source>) {
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
                .required(commandManager
                    .componentBuilder(BigDecimal::class.java, "amount")
                    .parser(AmountValidatorParser())
                    .defaultValue(DefaultValue.constant(BigDecimal.ONE))
                )
                .optional(commandManager
                    .componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
                ).handler { ctx ->
                    val sender: Player = ctx.sender().source()
                    val target: OfflinePlayer = ctx.get("target")
                    val amount: BigDecimal = ctx.get("amount")
                    val currency: String = ctx.get("currency")
                    payCommand(sender, target, amount, currency)
                }
            commandManager.command(payCmd)
            commandManager.command(commandManager.commandBuilder("pay").proxies(payCmd.build()))
        }
    }

    fun helpMessage(sender: CommandSender) {
        liteEco.locale.translationList("messages.help").forEach {
            sender.sendMessage(it)
        }
    }

    fun balanceCommand(sender: CommandSender, target: OfflinePlayer, currency: String) {
        if (!sender.hasPermission("lite.eco.balance.$currency") && !sender.hasPermission("lite.eco.balance.*"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))

        liteEco.pluginScope.launch {
            liteEco.api.getUserByUUID(target.uniqueId, currency).orElse(null)?.let { user ->
                val message = if (sender == target)
                    liteEco.locale.translation("messages.balance.format", helper.getComponentBal(user, currency))
                else
                    liteEco.locale.translation("messages.balance.format_target", helper.getComponentBal(user, currency))
                sender.sendMessage(message)
            } ?: sender.sendMessage(
                liteEco.locale.translation(
                    "messages.error.account_not_exist",
                    Placeholder.parsed("account", target.name.toString())
                )
            )
        }
    }

    fun balanceTopCommand(sender: CommandSender, page: Int, currency: String) {
        try {
            liteEco.pluginScope.launch {
                val topPlayers = io {
                    helper.getTopBalancesFormatted(currency)
                }

                val pagination = ComponentPaginator(topPlayers) {
                    selectedPage = page
                    itemsPerPage = 10
                    headerFormat = liteEco.locale.getMessage("messages.balance.top_header")
                    navigationFormat = liteEco.locale.getMessage("messages.balance.top_footer")
                }

                if (pagination.isAboveMaxPage(page)) {
                    sender.sendMessage(
                        liteEco.locale.translation("messages.error.maximum_page",
                            Placeholder.parsed("max_page", pagination.maxPages.toString())
                        )
                    )
                    return@launch
                }

                pagination.header("").let { sender.sendMessage(it) }
                pagination.display().forEach { content ->
                    sender.sendMessage(content)
                }
                sender.sendMessage(pagination.navigationBar("money top", currency))
            }
        } catch (e : Exception) {
            liteEco.logger.severe(e.message ?: e.localizedMessage)
            e.fillInStackTrace()
        }
    }

    fun payCommand(sender: Player, target: OfflinePlayer, amount: BigDecimal, currency: String) {
        if (!sender.hasPermission("lite.eco.pay.$currency") && !sender.hasPermission("lite.eco.pay.*"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.missing_currency_permission"))

        if (sender.uniqueId == target.uniqueId)
            return sender.sendMessage(liteEco.locale.translation("messages.error.self_pay"))

        economyPay.onPlayerPay(sender, target, amount, currency)
    }

}