package com.github.encryptsl.lite.eco.commands

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.ComponentPaginator
import com.github.encryptsl.lite.eco.commands.parsers.AmountValidatorParser
import com.github.encryptsl.lite.eco.commands.parsers.CurrencyParser
import com.github.encryptsl.lite.eco.commands.player.MoneyBalanceCmd
import com.github.encryptsl.lite.eco.commands.player.MoneyPayCmd
import com.github.encryptsl.lite.eco.commands.player.MoneyTopCmd
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

            commandManager.command(commandBuilder.literal("help").permission("lite.eco.money").handler { ctx ->
                helpMessage(ctx.sender().source())
            })

            val features = listOf(
                MoneyBalanceCmd(liteEco, helper),
                MoneyTopCmd(liteEco, helper),
                MoneyPayCmd(liteEco, economyPay)
            )

            features.forEach { it.register(commandManager, commandBuilder) }
        }
    }

    fun helpMessage(sender: CommandSender) {
        liteEco.locale.translationList("messages.help").forEach {
            sender.sendMessage(it)
        }
    }

}