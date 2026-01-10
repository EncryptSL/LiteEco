package com.github.encryptsl.lite.eco.commands.player

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.api.objects.ModernText
import com.github.encryptsl.lite.eco.commands.internal.CommandFeature
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.utils.Helper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.command.CommandSender
import org.incendo.cloud.Command
import org.incendo.cloud.component.DefaultValue
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.Source
import org.incendo.cloud.parser.standard.IntegerParser

class MoneyTopCmd(
    private val liteEco: LiteEco,
    private val helper: Helper
) : CommandFeature {
    override fun register(
        commandManager: PaperCommandManager<Source>,
        base: Command.Builder<Source>
    ) {
        val top = base.literal("top")
            .permission("lite.eco.top")
            .optional("page", IntegerParser.integerParser(1), DefaultValue.constant(1))
            .optional(
                commandManager.componentBuilder(String::class.java, "currency")
                    .parser(CurrencyParser())
                    .defaultValue(DefaultValue.parsed("dollars"))
            )
            .handler { ctx ->
                val sender = ctx.sender().source()
                val page: Int = ctx.get("page")
                val currency: String = ctx.get("currency")

                displayTopBalances(sender, page, currency)
            }

        commandManager.command(top)
        commandManager.command(commandManager.commandBuilder("baltop", "balancetop").proxies(top.build()))
    }

    private fun displayTopBalances(sender: CommandSender, page: Int, currency: String) {
        liteEco.pluginScope.launch {
            try {
                val topPlayers = withContext(Dispatchers.IO) {
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
                        liteEco.locale.translation(
                            "messages.error.maximum_page",
                            Placeholder.parsed("max_page", pagination.maxPages.toString())
                        )
                    )
                    return@launch
                }

                sender.sendMessage(pagination.header(""))
                pagination.display().forEach { content ->
                    sender.sendMessage(content)
                }
                sender.sendMessage(pagination.navigationBar("money top", currency))

            } catch (e: Exception) {
                liteEco.componentLogger.error("Failed to display balance top for ${sender.name}", e)
                sender.sendMessage(ModernText.miniModernText("<red>An internal error occurred while fetching the leaderboards."))
            }
        }
    }
}